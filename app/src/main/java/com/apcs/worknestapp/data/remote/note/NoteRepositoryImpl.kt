package com.apcs.worknestapp.data.remote.note

import android.util.Log
import com.apcs.worknestapp.utils.ColorUtils
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor() : NoteRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("NoteRepository", "Coroutine crashed", throwable)
    }
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + errorHandler)

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    override val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _currentNote = MutableStateFlow<Note?>(null)
    override val currentNote: StateFlow<Note?> = _currentNote.asStateFlow()

    private var notesListener: ListenerRegistration? = null

    init {
        auth.addAuthStateListener {
            val user = it.currentUser
            if (user == null) clearCache()
        }
    }

    private fun noteNotFound(noteId: String) {
        if (_currentNote.value?.docId == noteId) _currentNote.value = null
        _notes.update { list -> list.filterNot { it.docId == noteId } }
    }

    override fun removeListener() {
        notesListener?.remove()
        notesListener = null
    }

    override fun registerListener() {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val notesRef = firestore
            .collection("users")
            .document(authUser.uid)
            .collection("notes")

        notesListener?.remove() //Remove old listener
        notesListener = notesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("NoteRepository", "Listen notes snapshot failed", error)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.metadata.isFromCache) {
                val remoteNotes = snapshot.documents.mapNotNull {
                    it.toObject(Note::class.java)
                }
                val pendingNotes = _notes.value.filter { local ->
                    local.isLoading == true && remoteNotes.none { it.docId == local.docId }
                }
                _notes.value = remoteNotes + pendingNotes
            }
        }
    }

    override suspend fun refreshNotes() {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val notesRef = firestore
            .collection("users")
            .document(authUser.uid)
            .collection("notes")

        val snapshot = notesRef.get().await()
        val noteList = snapshot.documents.mapNotNull {
            it.toObject(Note::class.java)
        }
        _notes.value = noteList
    }

    override fun addNote(note: Note) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        repoScope.launch {
            val noteRef = firestore.collection("users")
                .document(authUser.uid)
                .collection("notes")
                .document()

            val noteId = noteRef.id

            withContext(Dispatchers.Main) {
                _notes.value = _notes.value + note.copy(docId = noteId, isLoading = true)
            }

            try {
                noteRef.set(note.copy(docId = noteId, isLoading = null)).await()
                val snapshot = noteRef.get().await()
                val newNote = snapshot.toObject(Note::class.java)

                withContext(Dispatchers.Main) {
                    newNote?.let { new ->
                        _notes.update { list ->
                            list.filterNot { it.docId == noteId } + new
                        }
                    }
                }
            } catch(e: Exception) {
                withContext(Dispatchers.Main) {
                    _notes.update { list ->
                        list.filterNot { it.docId == noteId }
                    }
                }
                throw e
            }
        }
    }

    override suspend fun getNote(docId: String): Note {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val noteRef = firestore.collection("users").document(authUser.uid)
            .collection("notes").document(docId)

        val noteDoc = noteRef.get().await()
        if (!noteDoc.exists()) throw Exception("Note not found")
        val note = noteDoc.toObject(Note::class.java) ?: throw Exception("Invalid note type")

        val checklistsSnapshot = noteRef.collection("checklists").get().await()
        val checklists = coroutineScope {
            checklistsSnapshot.documents.map { checklistDoc ->
                async {
                    try {
                        val checklist = checklistDoc.toObject(Checklist::class.java)
                        val tasksSnapshot = checklistDoc.reference.collection("tasks").get().await()
                        val tasks = tasksSnapshot.documents.mapNotNull {
                            it.toObject(Task::class.java)
                        }
                        checklist?.copy(tasks = tasks)
                    } catch(_: Exception) {
                        null
                    }
                }
            }.awaitAll().filterNotNull()
        }

        val result = note.copy(checklists = checklists)
        _currentNote.value = result
        return result
    }

    override fun deleteNote(docId: String) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        repoScope.launch {
            firestore.collection("users")
                .document(authUser.uid)
                .collection("notes")
                .document(docId)
                .delete()
                .await()

            withContext(Dispatchers.Main) {
                _notes.update { list ->
                    list.filterNot { it.docId == docId }
                }
                noteNotFound(docId)
            }
        }
    }

    override fun deleteNotes(noteIds: List<String>) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        repoScope.launch {
            val noteRef = firestore.collection("users")
                .document(authUser.uid)
                .collection("notes")

            val batch = firestore.batch()
            noteIds.forEach { id ->
                val docRef = noteRef.document(id)
                batch.delete(docRef)
            }
            batch.commit().await()

            withContext(Dispatchers.Main) {
                _notes.value = _notes.value.filterNot { it.docId in noteIds }
                if (_currentNote.value?.docId in noteIds) _currentNote.value = null
            }
        }
    }

    override fun deleteAllNotes() {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        repoScope.launch {
            val noteRef = firestore.collection("users")
                .document(authUser.uid)
                .collection("notes")

            val snapshot = noteRef.get().await()
            val batch = firestore.batch()
            snapshot.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()

            withContext(Dispatchers.Main) {
                _notes.value = emptyList()
                _currentNote.value = null
            }
        }
    }

    override fun deleteAllArchivedNotes(archived: Boolean) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        repoScope.launch {
            val noteRef = firestore.collection("users")
                .document(authUser.uid)
                .collection("notes")
                .whereEqualTo("archived", archived)

            val snapshot = noteRef.get().await()
            val batch = firestore.batch()
            snapshot.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()

            withContext(Dispatchers.Main) {
                _notes.update { list ->
                    list.filterNot { it.archived == archived }
                }
            }
        }
    }

    override fun archiveNotes(noteIds: List<String>, archived: Boolean) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        repoScope.launch {
            val noteRef = firestore.collection("users")
                .document(authUser.uid)
                .collection("notes")

            val batch = firestore.batch()
            noteIds.forEach { id ->
                val docRef = noteRef.document(id)
                batch.update(docRef, "archived", archived)
            }
            batch.commit().await()

            withContext(Dispatchers.Main) {
                _notes.update { list ->
                    list.map { if (it.docId in noteIds) it.copy(archived = archived) else it }
                }
            }
        }
    }

    override fun archiveAllNotes(archived: Boolean) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        repoScope.launch {
            val noteRef = firestore.collection("users")
                .document(authUser.uid)
                .collection("notes")

            val snapshot = noteRef
                .whereNotEqualTo("archived", archived)
                .get()
                .await()
            val batch = firestore.batch()
            snapshot.documents.forEach {
                batch.update(it.reference, "archived", archived)
            }
            batch.commit().await()

            withContext(Dispatchers.Main) {
                _notes.update { list ->
                    list.map { it.copy(archived = archived) }
                }
            }
        }
    }

    override fun archiveCompletedNotes() {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        repoScope.launch {
            val noteRef = firestore.collection("users")
                .document(authUser.uid)
                .collection("notes")

            val snapshot = noteRef
                .whereEqualTo("completed", true)
                .whereNotEqualTo("archived", true)
                .get()
                .await()
            val batch = firestore.batch()
            snapshot.documents.forEach {
                batch.update(it.reference, "archived", true)
            }
            batch.commit().await()

            withContext(Dispatchers.Main) {
                _notes.update { list ->
                    list.map { if (it.completed == true) it.copy(archived = true) else it }
                }
            }
        }
    }

    override fun addNewChecklist(noteId: String, checklist: Checklist) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val noteRef = firestore.collection("users")
            .document(authUser.uid)
            .collection("notes")
            .document(noteId)

        val checklistRef = noteRef.collection("checklists").document()
        val newChecklist = checklist.copy(docId = checklistRef.id)

        repoScope.launch {
            firestore.runTransaction { transaction ->
                val noteSnapshot = transaction.get(noteRef)
                if (!noteSnapshot.exists()) {
                    noteNotFound(noteId)
                    throw Exception("Note not found")
                }
                transaction.set(checklistRef, newChecklist)
            }.await()
            withContext(Dispatchers.Main) {
                if (_currentNote.value?.docId == noteId) {
                    _currentNote.update {
                        val currentChecklists = it?.checklists ?: emptyList()
                        it?.copy(checklists = currentChecklists + newChecklist)
                    }
                }
            }
        }
    }

    override fun deleteChecklist(noteId: String, checklistId: String) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val noteRef = firestore.collection("users")
            .document(authUser.uid)
            .collection("notes")
            .document(noteId)

        val checklistRef = noteRef
            .collection("checklists")
            .document(checklistId)

        repoScope.launch {
            firestore.runTransaction { transaction ->
                val noteSnapshot = transaction.get(noteRef)
                if (!noteSnapshot.exists()) {
                    noteNotFound(noteId)
                    throw Exception("Note not found")
                }
                transaction.delete(checklistRef)
            }.await()

            withContext(Dispatchers.Main) {
                if (_currentNote.value?.docId == noteId) {
                    _currentNote.update { note ->
                        val currentChecklists = note?.checklists ?: emptyList()
                        note?.copy(checklists = currentChecklists.filterNot { it.docId == checklistId })
                    }
                }
            }
        }
    }

    override fun updateChecklistName(noteId: String, checklistId: String, name: String) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val noteRef = firestore.collection("users")
            .document(authUser.uid)
            .collection("notes")
            .document(noteId)

        val checklistRef = noteRef
            .collection("checklists")
            .document(checklistId)

        repoScope.launch {
            firestore.runTransaction { transaction ->
                val noteSnapshot = transaction.get(noteRef)
                if (!noteSnapshot.exists()) {
                    noteNotFound(noteId)
                    throw Exception("Note not found")
                }
                transaction.update(checklistRef, "name", name)
            }.await()

            withContext(Dispatchers.Main) {
                if (_currentNote.value?.docId == noteId) {
                    _currentNote.update { note ->
                        val currentChecklists = (note?.checklists ?: emptyList()).map {
                            if (it.docId == checklistId) it.copy(name = name) else it
                        }
                        note?.copy(checklists = currentChecklists)
                    }
                }
            }
        }
    }

    override fun addNewTask(noteId: String, checklistId: String, task: Task) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val noteRef = firestore.collection("users")
            .document(authUser.uid)
            .collection("notes")
            .document(noteId)

        val checklistRef = noteRef.collection("checklists").document(checklistId)
        val taskRef = checklistRef.collection("tasks").document()
        val newTask = task.copy(docId = taskRef.id)

        repoScope.launch {
            firestore.runTransaction { transaction ->
                val noteSnapshot = transaction.get(noteRef)
                if (!noteSnapshot.exists()) {
                    noteNotFound(noteId)
                    throw Exception("Note not found when add task")
                }
                val checklistSnapshot = transaction.get(checklistRef)
                if (!checklistSnapshot.exists()) {
                    if (_currentNote.value?.docId == noteId) {
                        _currentNote.update { note ->
                            val currentChecklists = note?.checklists ?: emptyList()
                            note?.copy(checklists = currentChecklists.filterNot { it.docId == checklistId })
                        }
                    }
                    throw Exception("Checklist not found when add task")
                }
                transaction.set(taskRef, newTask)
            }.await()

            withContext(Dispatchers.Main) {
                if (_currentNote.value?.docId == noteId) {
                    _currentNote.update { note ->
                        note?.copy(
                            checklists = note.checklists.map { checklist ->
                                if (checklist.docId == checklistId) {
                                    checklist.copy(tasks = checklist.tasks + newTask)
                                } else checklist
                            }
                        )
                    }
                }
            }
        }
    }

    override fun deleteTask(noteId: String, checklistId: String, taskId: String) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        repoScope.launch {
            firestore.collection("users")
                .document(authUser.uid)
                .collection("notes")
                .document(noteId)
                .collection("checklists")
                .document(checklistId)
                .collection("tasks")
                .document(taskId)
                .delete()
                .await()

            withContext(Dispatchers.Main) {
                if (_currentNote.value?.docId == noteId) {
                    _currentNote.update { note ->
                        note?.copy(
                            checklists = note.checklists.map { checklist ->
                                if (checklist.docId == checklistId) {
                                    checklist.copy(
                                        tasks = checklist.tasks.filterNot { it.docId == taskId }
                                    )
                                } else checklist
                            }
                        )
                    }
                }
            }
        }
    }

    override fun updateTaskName(noteId: String, checklistId: String, taskId: String, name: String) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val noteRef = firestore.collection("users")
            .document(authUser.uid)
            .collection("notes")
            .document(noteId)

        val checklistRef = noteRef.collection("checklists").document(checklistId)
        val taskRef = checklistRef.collection("tasks").document(taskId)

        repoScope.launch {
            firestore.runTransaction { transaction ->
                val noteSnapshot = transaction.get(noteRef)
                if (!noteSnapshot.exists()) {
                    noteNotFound(noteId)
                    throw Exception("Note not found when update task name")
                }
                val checklistSnapshot = transaction.get(checklistRef)
                if (!checklistSnapshot.exists()) {
                    if (_currentNote.value?.docId == noteId) {
                        _currentNote.update { note ->
                            val currentChecklists = note?.checklists ?: emptyList()
                            note?.copy(checklists = currentChecklists.filterNot { it.docId == checklistId })
                        }
                    }
                    throw Exception("Checklist not found when update task name")
                }

                transaction.update(taskRef, "name", name)
            }.await()

            withContext(Dispatchers.Main) {
                if (_currentNote.value?.docId == noteId) {
                    _currentNote.update { note ->
                        note?.copy(
                            checklists = note.checklists.map { checklist ->
                                if (checklist.docId == checklistId) {
                                    checklist.copy(
                                        tasks = checklist.tasks.map { task ->
                                            if (task.docId == taskId) {
                                                task.copy(name = name)
                                            } else task
                                        }
                                    )
                                } else checklist
                            }
                        )
                    }
                }
            }
        }
    }

    override fun updateTaskDone(
        noteId: String,
        checklistId: String,
        taskId: String,
        done: Boolean,
    ) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val noteRef = firestore.collection("users")
            .document(authUser.uid)
            .collection("notes")
            .document(noteId)

        val checklistRef = noteRef.collection("checklists").document(checklistId)
        val taskRef = checklistRef.collection("tasks").document(taskId)

        repoScope.launch {
            firestore.runTransaction { transaction ->
                val noteSnapshot = transaction.get(noteRef)
                if (!noteSnapshot.exists()) {
                    noteNotFound(noteId)
                    throw Exception("Note not found when update task done")
                }
                val checklistSnapshot = transaction.get(checklistRef)
                if (!checklistSnapshot.exists()) {
                    if (_currentNote.value?.docId == noteId) {
                        _currentNote.update { note ->
                            val currentChecklists = note?.checklists ?: emptyList()
                            note?.copy(checklists = currentChecklists.filterNot { it.docId == checklistId })
                        }
                    }
                    throw Exception("Checklist not found update task done")
                }

                transaction.update(taskRef, "done", done)
            }.await()

            withContext(Dispatchers.Main) {
                if (_currentNote.value?.docId == noteId) {
                    _currentNote.update { note ->
                        note?.copy(
                            checklists = note.checklists.map { checklist ->
                                if (checklist.docId == checklistId) {
                                    checklist.copy(
                                        tasks = checklist.tasks.map { task ->
                                            if (task.docId == taskId) {
                                                task.copy(done = done)
                                            } else task
                                        }
                                    )
                                } else checklist
                            }
                        )
                    }
                }
            }
        }
    }


    override suspend fun updateNoteName(docId: String, name: String) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val noteRef = firestore.collection("users")
            .document(authUser.uid)
            .collection("notes")
            .document(docId)

        try {
            noteRef.update("name", name).await()
            _notes.update { list ->
                list.map { if (it.docId == docId) it.copy(name = name) else it }
            }
            if (_currentNote.value?.docId == docId) {
                _currentNote.update { it?.copy(name = name) }
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                noteNotFound(docId)
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    override suspend fun updateNoteCover(docId: String, color: Int?) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val noteRef = firestore.collection("users")
            .document(authUser.uid)
            .collection("notes")
            .document(docId)

        if (color != null && ColorUtils.safeParse(color) == null) throw Exception("Invalid color format")
        else {
            try {
                noteRef.update("cover", color).await()
                _notes.update { list ->
                    list.map { if (it.docId == docId) it.copy(cover = color) else it }
                }
                if (_currentNote.value?.docId == docId) {
                    _currentNote.update { it?.copy(cover = color) }
                }
            } catch(e: FirebaseFirestoreException) {
                if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                    noteNotFound(docId)
                }
                throw e
            } catch(e: Exception) {
                throw e
            }
        }
    }

    override suspend fun updateNoteDescription(docId: String, description: String) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val noteRef = firestore.collection("users")
            .document(authUser.uid)
            .collection("notes")
            .document(docId)

        try {
            noteRef.update("description", description).await()
            _notes.update { list ->
                list.map { if (it.docId == docId) it.copy(description = description) else it }
            }
            if (_currentNote.value?.docId == docId) {
                _currentNote.update { it?.copy(description = description) }
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                noteNotFound(docId)
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    override suspend fun updateNoteComplete(docId: String, newState: Boolean) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val noteRef = firestore.collection("users")
            .document(authUser.uid)
            .collection("notes")
            .document(docId)

        val updateNote =
            _notes.value.find { it.docId == docId } ?: throw Exception("Note not found")
        val previousState = updateNote.completed

        try {
            _notes.update { list ->
                list.map { if (it.docId == docId) it.copy(completed = newState) else it }
            }
            if (_currentNote.value?.docId == docId) {
                _currentNote.update { it?.copy(completed = newState) }
            }
            noteRef.update("completed", newState).await()
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                noteNotFound(docId)
            }
            throw e
        } catch(e: Exception) {
            _notes.update { list ->
                list.map { if (it.docId == docId) it.copy(completed = previousState) else it }
            }
            if (_currentNote.value?.docId == docId) {
                _currentNote.update { it?.copy(completed = previousState) }
            }
            throw e
        }
    }

    override suspend fun updateNoteArchive(docId: String, newState: Boolean) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val noteRef = firestore.collection("users")
            .document(authUser.uid)
            .collection("notes")
            .document(docId)

        val updateNote = _notes.value.find { it.docId == docId }
            ?: throw Exception("Note not found")
        val previousState = updateNote.archived

        try {
            _notes.update { list ->
                list.map { if (it.docId == docId) it.copy(archived = newState) else it }
            }
            if (_currentNote.value?.docId == docId) {
                _currentNote.update { it?.copy(archived = newState) }
            }

            noteRef.update("archived", newState).await()
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                noteNotFound(docId)
            }
            throw e
        } catch(e: Exception) {
            _notes.update { list ->
                list.map { if (it.docId == docId) it.copy(archived = previousState) else it }
            }
            if (_currentNote.value?.docId == docId) {
                _currentNote.update { it?.copy(archived = previousState) }
            }
            throw e
        }
    }

    override suspend fun updateNoteStartDate(docId: String, dateTime: Timestamp?) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val noteRef = firestore.collection("users")
            .document(authUser.uid)
            .collection("notes")
            .document(docId)

        try {
            noteRef.update("startDate", dateTime).await()
            _notes.update { list ->
                list.map { if (it.docId == docId) it.copy(startDate = dateTime) else it }
            }
            if (_currentNote.value?.docId == docId) {
                _currentNote.update { it?.copy(startDate = dateTime) }
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                noteNotFound(docId)
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    override suspend fun updateNoteEndDate(docId: String, dateTime: Timestamp?) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val noteRef = firestore.collection("users")
            .document(authUser.uid)
            .collection("notes")
            .document(docId)

        try {
            noteRef.update("endDate", dateTime).await()
            _notes.update { list ->
                list.map { if (it.docId == docId) it.copy(endDate = dateTime) else it }
            }
            if (_currentNote.value?.docId == docId) {
                _currentNote.update { it?.copy(endDate = dateTime) }
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                noteNotFound(docId)
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    override fun clearCache() {
        repoScope.coroutineContext.cancelChildren()
        removeListener()
        _notes.value = emptyList()
        _currentNote.value = null
    }
}

package com.apcs.worknestapp.data.remote.note

import com.apcs.worknestapp.utils.ColorUtils
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor() : NoteRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    override val notes: StateFlow<List<Note>> = _notes

    private var listenerRegistration: ListenerRegistration? = null

    override suspend fun refreshNotes() {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        listenerRegistration?.remove() //Remove old listener

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

    override suspend fun addNote(note: Note) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val noteRef = firestore.collection("users")
            .document(authUser.uid)
            .collection("notes")
            .add(note)
            .await()

        val snapshot = noteRef.get().await()
        val newNote = snapshot.toObject(Note::class.java)

        newNote?.let {
            _notes.value = _notes.value + newNote
        }
    }

    override suspend fun getNote(docId: String): Note {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val noteDoc = firestore.collection("users")
            .document(authUser.uid)
            .collection("notes")
            .document(docId)
            .get()
            .await()

        return noteDoc.toObject(Note::class.java) ?: throw Exception("Invalid note format")
    }

    override suspend fun deleteNote(docId: String) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        try {
            firestore.collection("users")
                .document(authUser.uid)
                .collection("notes")
                .document(docId)
                .delete()
                .await()

            _notes.update { list ->
                list.filterNot { it.docId == docId }
            }
        } catch(_: Exception) {
            _notes.update { list ->
                list.filterNot { it.docId == docId }
            }
        }
    }

    override suspend fun updateNoteCover(docId: String, color: Int?) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val noteRef = firestore.collection("users")
            .document(authUser.uid)
            .collection("notes")
            .document(docId)

        if (color != null && ColorUtils.safeParse(color) == null) {
            throw Exception("Invalid color format")
        } else {
            noteRef.update("cover", color).await()
            _notes.update { list ->
                list.map { if (it.docId == docId) it.copy(cover = color) else it }
            }
        }
    }

    override suspend fun updateNoteDescription(docId: String, description: String) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val noteRef = firestore.collection("users")
            .document(authUser.uid)
            .collection("notes")
            .document(docId)

        noteRef.update("description", description).await()
        _notes.update { list ->
            list.map { if (it.docId == docId) it.copy(description = description) else it }
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
            noteRef.update("completed", newState).await()
        } catch(e: Exception) {
            _notes.update { list ->
                list.map { if (it.docId == docId) it.copy(completed = previousState) else it }
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

        val updateNote =
            _notes.value.find { it.docId == docId } ?: throw Exception("Note not found")
        val previousState = updateNote.archived

        try {
            _notes.update { list ->
                list.map { if (it.docId == docId) it.copy(archived = newState) else it }
            }
            noteRef.update("archived", newState).await()
        } catch(e: Exception) {
            _notes.update { list ->
                list.map { if (it.docId == docId) it.copy(archived = previousState) else it }
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

        noteRef.update("startDate", dateTime).await()
        _notes.update { list ->
            list.map { if (it.docId == docId) it.copy(startDate = dateTime) else it }
        }
    }

    override suspend fun updateNoteEndDate(docId: String, dateTime: Timestamp?) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val noteRef = firestore.collection("users")
            .document(authUser.uid)
            .collection("notes")
            .document(docId)

        noteRef.update("endDate", dateTime).await()
        _notes.update { list ->
            list.map { if (it.docId == docId) it.copy(endDate = dateTime) else it }
        }
    }

    override fun removeListener() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    override fun clearCache() {
        _notes.value = emptyList()
    }
}

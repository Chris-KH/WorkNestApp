package com.apcs.worknestapp.data.remote.note

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
        } catch(e: Exception) {
            _notes.update { list ->
                list.filterNot { it.docId == docId }
            }
        }
    }

    override suspend fun updateNoteComplete(docId: String, newState: Boolean) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        try {
            val noteRef = firestore.collection("users")
                .document(authUser.uid)
                .collection("notes")
                .document(docId)

            noteRef.update("completed", newState).await()

            _notes.update { list ->
                list.map { if (it.docId == docId) it.copy(completed = newState) else it }
            }
        } catch(e: Exception) {
            _notes.update { list ->
                list.filterNot { it.docId == docId }
            }
            throw e
        }
    }

    override suspend fun updateNoteArchive(docId: String, newState: Boolean) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        try {
            val noteRef = firestore.collection("users")
                .document(authUser.uid)
                .collection("notes")
                .document(docId)

            noteRef.update("archived", newState).await()

            _notes.update { list ->
                list.map { if (it.docId == docId) it.copy(archived = newState) else it }
            }
        } catch(e: Exception) {
            _notes.update { list ->
                list.filterNot { it.docId == docId }
            }
            throw e
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

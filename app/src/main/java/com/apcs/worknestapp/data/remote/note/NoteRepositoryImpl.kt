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

    override suspend fun deleteNote(docId: String) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        firestore.collection("users")
            .document(authUser.uid)
            .collection("notes")
            .document(docId)
            .delete()
            .await()

        _notes.update { list ->
            list.filterNot { it.docId == docId }
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

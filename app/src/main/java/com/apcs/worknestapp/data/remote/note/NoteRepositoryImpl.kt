package com.apcs.worknestapp.data.remote.note

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor() : NoteRepository {
    private val auth = FirebaseAuth.getInstance()
    private val store = FirebaseFirestore.getInstance()

    private val _notes = MutableStateFlow(emptyList<Note>())
    override val notes: StateFlow<List<Note>> = _notes

    override fun clearCache() {
        _notes.value = emptyList()
    }
}

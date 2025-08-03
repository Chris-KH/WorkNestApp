package com.apcs.worknestapp.data.remote.note

import kotlinx.coroutines.flow.StateFlow

interface NoteRepository {
    val notes: StateFlow<List<Note>>

    suspend fun refreshNotes()
    suspend fun getNote(docId: String): Note
    suspend fun addNote(note: Note)
    suspend fun deleteNote(docId: String)
    suspend fun updateNoteComplete(docId: String, newState: Boolean)
    suspend fun updateNoteArchive(docId: String, newState: Boolean)
    fun removeListener()
    fun clearCache()
}

package com.apcs.worknestapp.data.remote.note

import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.StateFlow

interface NoteRepository {
    val notes: StateFlow<List<Note>>

    suspend fun refreshNotes()
    suspend fun getNote(docId: String): Note
    suspend fun addNote(note: Note)
    suspend fun deleteNote(docId: String)
    suspend fun deleteAllNote()
    suspend fun updateNoteName(docId: String, name: String)
    suspend fun updateNoteCover(docId: String, color: Int?)
    suspend fun updateNoteDescription(docId: String, description: String)
    suspend fun updateNoteComplete(docId: String, newState: Boolean)
    suspend fun updateNoteArchive(docId: String, newState: Boolean)
    suspend fun updateNoteStartDate(docId: String, dateTime: Timestamp?)
    suspend fun updateNoteEndDate(docId: String, dateTime: Timestamp?)
    fun removeListener()
    fun clearCache()
}

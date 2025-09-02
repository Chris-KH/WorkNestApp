package com.apcs.worknestapp.data.remote.note

import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.StateFlow

interface NoteRepository {
    val notes: StateFlow<List<Note>>

    fun removeListener()
    fun registerListener()
    suspend fun refreshNotes()
    suspend fun getNote(docId: String): Note
    fun addNote(note: Note)
    fun deleteNote(docId: String)
    fun deleteNotes(noteIds: List<String>)
    fun deleteAllNotes()
    fun deleteAllArchivedNotes(archived: Boolean)
    fun archiveNotes(noteIds: List<String>)
    fun archiveAllNotes()
    fun archiveCompletedNotes()
    suspend fun updateNoteName(docId: String, name: String)
    suspend fun updateNoteCover(docId: String, color: Int?)
    suspend fun updateNoteDescription(docId: String, description: String)
    suspend fun updateNoteComplete(docId: String, newState: Boolean)
    suspend fun updateNoteArchive(docId: String, newState: Boolean)
    suspend fun updateNoteStartDate(docId: String, dateTime: Timestamp?)
    suspend fun updateNoteEndDate(docId: String, dateTime: Timestamp?)
    fun clearCache()
}

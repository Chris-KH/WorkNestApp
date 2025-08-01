package com.apcs.worknestapp.data.remote.note

import kotlinx.coroutines.flow.StateFlow

interface NoteRepository {
    val notes: StateFlow<List<Note>>

    suspend fun refreshNotes()
    suspend fun deleteNote(docId: String)
    fun removeListener()
    fun clearCache()
}

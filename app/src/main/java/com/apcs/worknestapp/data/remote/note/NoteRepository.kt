package com.apcs.worknestapp.data.remote.note

import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.StateFlow

interface NoteRepository {
    val notes: StateFlow<List<Note>>
    val currentNote: StateFlow<Note?>

    fun removeListener()
    fun registerListener()
    suspend fun refreshNotes()
    suspend fun getNote(docId: String): Note
    fun addNote(note: Note)
    fun deleteNote(docId: String)
    fun deleteNotes(noteIds: List<String>)
    fun deleteAllNotes()
    fun deleteAllArchivedNotes(archived: Boolean)
    fun archiveNotes(noteIds: List<String>, archived: Boolean)
    fun archiveAllNotes(archived: Boolean)
    fun archiveCompletedNotes()

    //Checklist in note
    fun addNewChecklist(noteId: String, checklist: Checklist)
    fun deleteChecklist(noteId: String, checklistId: String)
    fun updateChecklistName(noteId: String, checklistId: String, name: String)
    fun addNewTask(noteId: String, checklistId: String, task: Task)
    fun deleteTask(noteId: String, checklistId: String, taskId: String)
    fun updateTaskName(noteId: String, checklistId: String, taskId: String, name: String)
    fun updateTaskDone(noteId: String, checklistId: String, taskId: String, done: Boolean)

    //Update note
    suspend fun updateNoteName(docId: String, name: String)
    suspend fun updateNoteCover(docId: String, color: Int?)
    suspend fun updateNoteDescription(docId: String, description: String)
    suspend fun updateNoteComplete(docId: String, newState: Boolean)
    suspend fun updateNoteArchive(docId: String, newState: Boolean)
    suspend fun updateNoteStartDate(docId: String, dateTime: Timestamp?)
    suspend fun updateNoteEndDate(docId: String, dateTime: Timestamp?)
    fun clearCache()
}

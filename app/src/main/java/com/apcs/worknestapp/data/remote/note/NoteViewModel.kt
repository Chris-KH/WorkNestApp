package com.apcs.worknestapp.data.remote.note

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val noteRepo: NoteRepository,
) : ViewModel() {
    val notes = noteRepo.notes
    val currentNote = noteRepo.currentNote


    fun registerNotesListener() {
        noteRepo.registerNotesListener()
    }

    fun removeNotesListener() {
        noteRepo.removeNotesListener()
    }

    fun registerCurrentNoteListener(noteId: String) {
        noteRepo.registerCurrentNoteListener(noteId)
    }

    fun removeCurrentNoteListener() {
        noteRepo.removeCurrentNoteListener()
    }

    suspend fun refreshNotesIfEmpty(): Boolean {
        if (notes.value.isEmpty()) return refreshNotes()
        return true
    }

    suspend fun refreshNotes(): Boolean {
        return try {
            noteRepo.refreshNotes()
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Refresh notes failed", e)
            false
        }
    }

    suspend fun getNote(docId: String): Note? {
        return try {
            noteRepo.getNote(docId)
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Get note $docId failed", e)
            null
        }
    }

    fun addNote(note: Note): Boolean {
        return try {
            noteRepo.addNote(note)
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Add a note failed", e)
            false
        }
    }

    fun deleteNote(docId: String): Boolean {
        return try {
            noteRepo.deleteNote(docId)
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Delete note $docId failed", e)
            false
        }
    }

    fun deleteNotes(noteIds: List<String>): Boolean {
        return try {
            noteRepo.deleteNotes(noteIds)
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Delete list of note failed", e)
            false
        }
    }

    fun deleteAllNotes(): Boolean {
        return try {
            noteRepo.deleteAllNotes()
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Delete all notes failed", e)
            false
        }
    }

    fun deleteAllArchivedNotes(archived: Boolean): Boolean {
        return try {
            noteRepo.deleteAllArchivedNotes(archived)
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Delete all archived=$archived notes failed", e)
            false
        }
    }

    fun archiveNotes(noteIds: List<String>, archived: Boolean): Boolean {
        return try {
            noteRepo.archiveNotes(noteIds, archived)
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Archive list of note failed", e)
            false
        }
    }

    fun archiveAllNotes(archived: Boolean): Boolean {
        return try {
            noteRepo.archiveAllNotes(archived)
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Archive all notes failed", e)
            false
        }
    }

    fun archiveCompletedNotes(): Boolean {
        return try {
            noteRepo.archiveCompletedNotes()
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Archive completed notes failed", e)
            false
        }
    }

    suspend fun addNewChecklist(noteId: String, checklist: Checklist = Checklist()): Boolean {
        return try {
            noteRepo.addNewChecklist(noteId, checklist)
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Add new checklist failed", e)
            false
        }
    }

    suspend fun deleteChecklist(noteId: String, checklistId: String): Boolean {
        return try {
            noteRepo.deleteChecklist(noteId, checklistId)
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Delete checklist failed", e)
            false
        }
    }

    suspend fun updateChecklistName(noteId: String, checklistId: String, name: String): Boolean {
        return try {
            noteRepo.updateChecklistName(noteId, checklistId, name)
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Update checklist name failed", e)
            false
        }
    }

    suspend fun addNewTask(noteId: String, checklistId: String, task: Task): Boolean {
        return try {
            noteRepo.addNewTask(noteId, checklistId, task)
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Add new failed", e)
            false
        }
    }

    suspend fun deleteTask(noteId: String, checklistId: String, taskId: String): Boolean {
        return try {
            noteRepo.deleteTask(noteId, checklistId, taskId)
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Delete task failed", e)
            false
        }
    }

    suspend fun addComment(noteId: String, comment: Comment): Boolean {
        return try {
            noteRepo.addComment(noteId, comment)
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Add comment failed", e)
            false
        }
    }

    suspend fun deleteComment(noteId: String, commentId: String): Boolean {
        return try {
            noteRepo.deleteComment(noteId, commentId)
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "delete comment failed", e)
            false
        }
    }

    suspend fun updateTaskName(
        noteId: String,
        checklistId: String,
        taskId: String,
        name: String,
    ): Boolean {
        return try {
            noteRepo.updateTaskName(noteId, checklistId, taskId, name)
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Update task name failed", e)
            false
        }
    }

    suspend fun updateTaskDone(
        noteId: String,
        checklistId: String,
        taskId: String,
        done: Boolean,
    ): Boolean {
        return try {
            noteRepo.updateTaskDone(noteId, checklistId, taskId, done)
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Update task done failed", e)
            false
        }
    }

    suspend fun updateNoteName(docId: String, name: String): Boolean {
        return try {
            noteRepo.updateNoteName(docId, name)
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Update note name failed", e)
            false
        }
    }

    suspend fun updateNoteCover(docId: String, color: Int?): Boolean {
        return try {
            noteRepo.updateNoteCover(docId, color)
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Update note cover failed", e)
            false
        }
    }

    suspend fun updateNoteDescription(docId: String, description: String): Boolean {
        return try {
            noteRepo.updateNoteDescription(docId, description)
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Update note description failed", e)
            false
        }
    }

    suspend fun updateNoteComplete(docId: String, newState: Boolean): Boolean {
        return try {
            noteRepo.updateNoteComplete(docId, newState)
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Update note complete failed", e)
            false
        }
    }

    suspend fun updateNoteArchive(docId: String, newState: Boolean): Boolean {
        return try {
            noteRepo.updateNoteArchive(docId, newState)
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Update note archive failed", e)
            false
        }
    }

    suspend fun updateNoteStartDate(docId: String, dateTime: Timestamp?): Boolean {
        return try {
            noteRepo.updateNoteStartDate(docId, dateTime)
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Update note start date failed", e)
            false
        }
    }

    suspend fun updateNoteEndDate(docId: String, dateTime: Timestamp?): Boolean {
        return try {
            noteRepo.updateNoteEndDate(docId, dateTime)
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Update note end date failed", e)
            false
        }
    }
}

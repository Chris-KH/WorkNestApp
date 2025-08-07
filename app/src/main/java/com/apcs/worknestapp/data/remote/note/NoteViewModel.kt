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

    fun removeListener() {
        noteRepo.removeListener()
    }

    fun registerListener() {
        try {
            noteRepo.registerListener()
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Register listener for notes failed", e)
        }
    }

    suspend fun refreshNotesIfEmpty(): Boolean {
        if (notes.value.isEmpty()) {
            return refreshNotes()
        }

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

    suspend fun addNote(note: Note): Boolean {
        return try {
            noteRepo.addNote(note)
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Add a note failed", e)
            false
        }
    }

    suspend fun deleteNote(docId: String): Boolean {
        return try {
            noteRepo.deleteNote(docId)
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Delete note $docId failed", e)
            false
        }
    }

    suspend fun deleteAllNotes(): Boolean {
        return try {
            noteRepo.deleteAllNotes()
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Delete all notes failed", e)
            false
        }
    }

    suspend fun archiveAllNotes(): Boolean {
        return try {
            noteRepo.archiveAllNotes()
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Archive all notes failed", e)
            false
        }
    }

    suspend fun archiveCompletedNotes(): Boolean {
        return try {
            noteRepo.archiveCompletedNotes()
            true
        } catch(e: Exception) {
            Log.e("NoteViewModel", "Archive completed notes failed", e)
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

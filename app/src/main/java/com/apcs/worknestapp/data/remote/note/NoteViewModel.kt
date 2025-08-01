package com.apcs.worknestapp.data.remote.note

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val noteRepo: NoteRepository,
) : ViewModel() {
    val notes = noteRepo.notes

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
}

package com.apcs.worknestapp.data.remote.message

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val messageRepo: MessageRepository,
) : ViewModel() {
    val conservations = messageRepo.conservations

    suspend fun loadConservations(): Boolean {
        return try {
            messageRepo.loadConservations()
            true
        } catch(e: Exception) {
            Log.e("MessageViewModel", "Load conservations failed", e)
            false
        }
    }

    suspend fun loadConservationsIfEmpty(): Boolean {
        if (conservations.value.isEmpty()) return loadConservations()
        return true
    }

    suspend fun updateConservationSeen(docId: String, state: Boolean): Boolean {
        return try {
            messageRepo.updateConservationSeen(docId, state)
            true
        } catch(e: Exception) {
            Log.e("MessageViewModel", "Update conservation seen failed", e)
            false
        }
    }

}

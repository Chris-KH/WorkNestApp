package com.apcs.worknestapp.data.remote.message

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val messageRepo: MessageRepository,
) : ViewModel() {
    val conservations = messageRepo.conservations
    val currentConservation = messageRepo.currentConservation

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

    fun getCacheConservation(docId: String?): Boolean {
        return try {
            messageRepo.getCacheConservation(docId)
            true
        } catch(e: Exception) {
            Log.e("MessageViewModel", "Get cache conservation failed", e)
            false
        }
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

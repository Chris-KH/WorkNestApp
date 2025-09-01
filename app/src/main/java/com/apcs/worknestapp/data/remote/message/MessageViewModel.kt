package com.apcs.worknestapp.data.remote.message

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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

    fun getConservation(docId: String?): Boolean {
        return try {
            messageRepo.getConservation(docId)
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

    fun registerConservationListener() {
        messageRepo.registerConservationListener()
    }

    fun removeConservationListener() {
        messageRepo.removeConservationListener()
    }

    fun registerMessageListener(conservationId: String) {
        messageRepo.registerMessageListener(conservationId)
    }

    fun removeMessageListener(conservationId: String) {
        messageRepo.removeMessageListener(conservationId)
    }

    suspend fun loadNewMessages(conservationId: String): Boolean {
        return try {
            messageRepo.loadNewMessages(conservationId)
            true
        } catch(e: Exception) {
            Log.e("MessageViewModel", "Load new messages failed", e)
            false
        }
    }

    fun sendMessage(conservationId: String, message: Message) {
        viewModelScope.launch {
            try {
                messageRepo.sendMessage(conservationId, message)
            } catch(e: Exception) {
                Log.e("MessageViewModel", "Send message failed", e)
            }
        }
    }
}

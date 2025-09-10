package com.apcs.worknestapp.data.remote.message

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apcs.worknestapp.data.remote.user.User
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

    fun createConservation(conservation: Conservation, userMetadata: User): Boolean {
        return try {
            messageRepo.createConservation(conservation, userMetadata)
            true
        } catch(e: Exception) {
            Log.e("MessageViewModel", "Create conservation failed", e)
            false
        }
    }

    fun getConservation(docId: String?): Conservation? {
        return try {
            messageRepo.getConservation(docId)
        } catch(e: Exception) {
            Log.e("MessageViewModel", "Get cache conservation failed", e)
            null
        }
    }

    fun getConservationWith(userId: String): Conservation? {
        return try {
            messageRepo.getConservationWith(userId)
        } catch(e: Exception) {
            Log.e("MessageViewModel", "Get cache conservation with user failed", e)
            null
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

    fun registerCurrentConservationListener(conservationId: String) {
        messageRepo.registerCurrentConservationListener(conservationId)
    }

    fun removeCurrentConservationListener() {
        messageRepo.removeCurrentConservationListener()
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

    suspend fun deleteConservation(conservationId: String): Boolean {
        return try {
            messageRepo.deleteConservation(conservationId)
            true
        } catch(e: Exception) {
            Log.e("MessageViewModel", "Delete conservation failed", e)
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

    suspend fun deleteMessage(
        conservationId: String,
        messageId: String,
        isForMe: Boolean,
    ): Boolean {
        return try {
            messageRepo.deleteMessage(conservationId, messageId, isForMe)
            true
        } catch(e: Exception) {
            Log.e("MessageViewModel", "Delete message failed", e)
            false
        }
    }
}

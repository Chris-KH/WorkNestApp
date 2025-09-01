package com.apcs.worknestapp.data.remote.message

import com.apcs.worknestapp.data.remote.user.User
import kotlinx.coroutines.flow.StateFlow

interface MessageRepository {
    val conservations: StateFlow<List<Conservation>>
    val currentConservation: StateFlow<Conservation?>

    fun registerConservationListener()
    fun removeConservationListener()
    fun registerMessageListener(conservationId: String)
    fun removeMessageListener(conservationId: String)
    fun getConservation(docId: String?): Conservation?
    fun getConservationWith(userId: String): Conservation?
    fun createConservation(conservation: Conservation, userMetadata: User)
    suspend fun loadConservations()
    suspend fun deleteConservation(docId: String)
    suspend fun updateConservationSeen(docId: String, state: Boolean)
    suspend fun loadNewMessages(conservationId: String)
    suspend fun sendMessage(conservationId: String, message: Message)
    suspend fun deleteMessage(conservationId: String, messageId: String)
    fun clearCache()
}

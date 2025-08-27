package com.apcs.worknestapp.data.remote.message

import kotlinx.coroutines.flow.StateFlow

interface MessageRepository {
    val conservations: StateFlow<List<Conservation>>
    val currentConservation: StateFlow<Conservation?>

    fun removeListener()
    fun registerConservationListener()
    fun getConservation(docId: String?)
    suspend fun loadConservations()
    suspend fun updateConservationSeen(docId: String, state: Boolean)
    suspend fun loadNewMessages(conservationId: String)
    suspend fun sendMessage(conservationId: String, message: Message)
    suspend fun deleteMessage(conservationId: String, messageId: String)
    fun clearCache()
}

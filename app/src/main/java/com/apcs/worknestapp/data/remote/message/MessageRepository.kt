package com.apcs.worknestapp.data.remote.message

import kotlinx.coroutines.flow.StateFlow

interface MessageRepository {
    val conservations: StateFlow<List<Conservation>>

    fun removeListener()
    fun registerConservationListener()
    suspend fun loadConservations()
    suspend fun updateConservationSeen(docId: String, state: Boolean)
    fun clearCache()
}

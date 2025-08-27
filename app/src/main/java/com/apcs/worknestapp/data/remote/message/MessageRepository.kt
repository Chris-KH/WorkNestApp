package com.apcs.worknestapp.data.remote.message

import kotlinx.coroutines.flow.StateFlow

interface MessageRepository {
    val conservations: StateFlow<List<Conservation>>
    val currentConservation: StateFlow<Conservation?>

    fun removeListener()
    fun registerConservationListener()
    fun getCacheConservation(docId: String?)
    suspend fun loadConservations()
    suspend fun updateConservationSeen(docId: String, state: Boolean)
    fun clearCache()
}

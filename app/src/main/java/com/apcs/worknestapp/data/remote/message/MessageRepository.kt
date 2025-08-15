package com.apcs.worknestapp.data.remote.message

import kotlinx.coroutines.flow.StateFlow

interface MessageRepository {
    val conservations: StateFlow<List<Conservation>>

    fun clearCache()
}

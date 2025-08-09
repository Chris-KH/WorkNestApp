package com.apcs.worknestapp.data.remote.message

import kotlinx.coroutines.flow.StateFlow

interface MessageRepository {
    val messageMap: StateFlow<Map<String, List<Message>>>

    fun clearCache()
}

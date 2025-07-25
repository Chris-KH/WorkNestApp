package com.apcs.worknestapp.data.remote.notification

import kotlinx.coroutines.flow.StateFlow

interface NotificationRepository {
    val notifications: StateFlow<List<Notification>>

    suspend fun refreshNotifications()
    suspend fun deleteNotification(docId: String)
    suspend fun markRead(docId: String)
    suspend fun markAllRead()
}

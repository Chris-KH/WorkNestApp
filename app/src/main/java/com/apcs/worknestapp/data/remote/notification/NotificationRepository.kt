package com.apcs.worknestapp.data.remote.notification

import kotlinx.coroutines.flow.StateFlow

interface NotificationRepository {
    val notifications: StateFlow<List<Notification>>

    fun registerNotificationsListener()
    fun removeNotificationsListener()
    suspend fun refreshNotifications()
    suspend fun deleteNotification(docId: String)
    suspend fun markRead(docId: String, read: Boolean)
    suspend fun markAllRead()

    fun clearCache()
}

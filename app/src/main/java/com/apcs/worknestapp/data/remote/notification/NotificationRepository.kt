package com.apcs.worknestapp.data.remote.notification

import kotlinx.coroutines.flow.StateFlow

interface NotificationRepository {
    val notifications: StateFlow<List<Notification>>

    suspend fun refreshNotifications()
}

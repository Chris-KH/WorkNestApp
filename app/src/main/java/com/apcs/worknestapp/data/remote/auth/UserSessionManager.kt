package com.apcs.worknestapp.data.remote.auth

import com.apcs.worknestapp.data.remote.notification.NotificationRepository
import javax.inject.Inject

class UserSessionManager @Inject constructor(
    private val notificationRepo: NotificationRepository,
) {
    fun signOutAndClearAll() {
        notificationRepo.clearCache()
    }
}

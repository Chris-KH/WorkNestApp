package com.apcs.worknestapp.data.remote.notification

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepo: NotificationRepository,
) : ViewModel() {
    val notifications = notificationRepo.notifications

    suspend fun refreshNotifications() {
        notificationRepo.refreshNotifications()
    }
}

package com.apcs.worknestapp.data.remote.notification

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepo: NotificationRepository,
) : ViewModel() {
    val notifications = notificationRepo.notifications

    suspend fun refreshNotificationsIfEmpty(): Boolean {
        if (notifications.value.isEmpty()) {
            return refreshNotifications()
        }
        return true
    }

    suspend fun refreshNotifications(): Boolean {
        return try {
            notificationRepo.refreshNotifications()
            true
        } catch(e: Exception) {
            Log.e("NotificationViewModel", "Refresh notifications failed", e)
            false
        }
    }

    suspend fun deleteNotification(docId: String): Boolean {
        return try {
            notificationRepo.deleteNotification(docId)
            true
        } catch(e: Exception) {
            Log.e("NotificationViewModel", "Delete notification failed", e)
            false
        }
    }

    suspend fun markRead(docId: String, read: Boolean): Boolean {
        return try {
            notificationRepo.markRead(docId, read)
            true
        } catch(e: Exception) {
            Log.e("NotificationViewModel", "Mark read notification $docId failed", e)
            false
        }
    }

    suspend fun markAllRead(): Boolean {
        return try {
            notificationRepo.markAllRead()
            true
        } catch(e: Exception) {
            Log.e("NotificationViewModel", "Mark all read notification failed", e)
            false
        }
    }
}

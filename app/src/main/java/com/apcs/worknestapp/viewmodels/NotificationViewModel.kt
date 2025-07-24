package com.apcs.worknestapp.viewmodels

import androidx.lifecycle.ViewModel
import com.apcs.worknestapp.models.Notification
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class NotificationViewModel @Inject constructor(
) : ViewModel() {
    private val _notifications = MutableStateFlow<List<Notification>>(
        List(size = 0) { idx ->
            Notification(
                docId = idx.toString(),
                title = "Notify",
                message = "Hello bro",
                isRead = Random.nextBoolean(),
                createdAt = Timestamp.now()
            )
        }
    )
    val notifications: StateFlow<List<Notification>> = _notifications

    suspend fun refreshNotifications() {
        delay(5000)
    }
}

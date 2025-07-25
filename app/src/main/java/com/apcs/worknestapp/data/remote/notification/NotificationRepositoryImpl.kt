package com.apcs.worknestapp.data.remote.notification

import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import kotlin.random.Random

class NotificationRepositoryImpl @Inject constructor() : NotificationRepository {
    private val _notifications = MutableStateFlow(
        List(size = 0) { idx ->
            Notification(
                docId = idx.toString(),
                title = "Notify",
                message = "Hello bro",
                isRead = Random.Default.nextBoolean(),
                createdAt = Timestamp.Companion.now()
            )
        }
    )
    override val notifications: StateFlow<List<Notification>> = _notifications

    override suspend fun refreshNotifications() {
        delay(5000)
    }
}

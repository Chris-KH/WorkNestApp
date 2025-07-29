package com.apcs.worknestapp.data.remote.notification

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.random.Random

class NotificationRepositoryImpl @Inject constructor() : NotificationRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _notifications = MutableStateFlow(
        List(size = 50) { idx ->
            Notification(
                docId = idx.toString(),
                title = "Notify",
                message = "Hello bro asd asd asd wq dqw d as das d qw edqw q fgakdfjna " +
                        "jashjd jklash aklsjhdjka shdjakshkjd ashdkjh kjahsdj oqwjo wka " +
                        "hakj hdakjskjash dajkskaska a a a jakshd kjashdjka s",
                isRead = Random.Default.nextBoolean(),
                createdAt = Timestamp.now()
            )
        })
    override val notifications: StateFlow<List<Notification>> = _notifications

    override suspend fun refreshNotifications() {
        val authUser = auth.currentUser ?: throw Exception("No user logged in")

        val notificationsRef = firestore
            .collection("users")
            .document(authUser.uid)
            .collection("notifications")
//            .orderBy("createdAt", Query.Direction.DESCENDING)
//            .limit(20)

        val snapshot = notificationsRef.get().await()

        delay(3000)

        _notifications.value = List(size = 50) { idx ->
            Notification(
                docId = idx.toString(),
                title = "Notify",
                message = "Hello bro",
                isRead = Random.Default.nextBoolean(),
                createdAt = Timestamp.now()
            )
        }
    }

    override suspend fun deleteNotification(docId: String) {
        val authUser = auth.currentUser ?: throw Exception("No user logged in")

        firestore.collection("users").document(authUser.uid).collection("notifications")
            .document(docId).delete().await()

        _notifications.update { list ->
            list.filterNot { it.docId == docId }
        }
    }

    override suspend fun markRead(docId: String) {
        val authUser = auth.currentUser ?: throw Exception("No user logged in")

        val notificationRef =
            firestore.collection("users").document(authUser.uid).collection("notifications")

        val docRef = notificationRef.document(docId)
        docRef.update("isRead", true).await()

        _notifications.update { list ->
            list.map { if (it.docId == docId) it.copy(isRead = true) else it }
        }
    }

    override suspend fun markAllRead() {
        val authUser = auth.currentUser ?: throw Exception("No user logged in")

        val notificationsRef =
            firestore.collection("users").document(authUser.uid).collection("notifications")

        val snapshot = notificationsRef.get().await()
        val batch = firestore.batch()
        for(doc in snapshot.documents) {
            val docRef = notificationsRef.document(doc.id)
            batch.update(docRef, "isRead", true)
        }

        batch.commit().await()
        _notifications.update { list ->
            list.map { it.copy(isRead = true) }
        }
    }

    override fun clearCache() {
        _notifications.value = emptyList()
    }
}

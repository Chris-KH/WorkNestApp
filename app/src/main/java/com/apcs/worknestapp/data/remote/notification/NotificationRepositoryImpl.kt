package com.apcs.worknestapp.data.remote.notification

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor() : NotificationRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
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
        _notifications.value = snapshot.documents.mapNotNull {
            it.toObject(Notification::class.java)
        }
        Log.d("NotificationRepository", _notifications.value.toString())
    }

    override suspend fun deleteNotification(docId: String) {
        val authUser = auth.currentUser ?: throw Exception("No user logged in")

        firestore.collection("users")
            .document(authUser.uid)
            .collection("notifications")
            .document(docId)
            .delete()
            .await()

        _notifications.update { list ->
            list.filterNot { it.docId == docId }
        }
    }

    override suspend fun markRead(docId: String) {
        val authUser = auth.currentUser ?: throw Exception("No user logged in")

        val notificationRef = firestore.collection("users")
            .document(authUser.uid)
            .collection("notifications")

        val docRef = notificationRef.document(docId)
        docRef.update("read", true).await()

        _notifications.update { list ->
            list.map { if (it.docId == docId) it.copy(read = true) else it }
        }
    }

    override suspend fun markAllRead() {
        val authUser = auth.currentUser ?: throw Exception("No user logged in")

        val notificationsRef = firestore.collection("users")
            .document(authUser.uid)
            .collection("notifications")

        val snapshot = notificationsRef.get().await()
        val batch = firestore.batch()
        for(doc in snapshot.documents) {
            val docRef = notificationsRef.document(doc.id)
            batch.update(docRef, "read", true)
        }

        batch.commit().await()
        _notifications.update { list ->
            list.map { it.copy(read = true) }
        }
    }

    override fun clearCache() {
        _notifications.value = emptyList()
    }
}

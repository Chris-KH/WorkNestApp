package com.apcs.worknestapp.data.remote.notification

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
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

    private var listenerRegistration: ListenerRegistration? = null

    init {
        auth.addAuthStateListener {
            val user = it.currentUser
            if (user == null) clearCache()
            else {
                removeNotificationsListener()
                registerNotificationsListener()
            }
        }
    }

    override fun registerNotificationsListener() {
        val authUser = auth.currentUser
        if (authUser == null) {
            removeNotificationsListener()
            return
        }
        removeNotificationsListener()

        val notificationsRef = firestore
            .collection("users")
            .document(authUser.uid)
            .collection("notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)

        listenerRegistration = notificationsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("NotificationRepository", "Listen notifications failed", error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val notificationList = snapshot.documents.mapNotNull {
                    it.toObject(Notification::class.java)
                }
                _notifications.value = notificationList
            }
        }
    }

    override fun removeNotificationsListener() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    override suspend fun refreshNotifications() {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val notificationsRef = firestore
            .collection("users")
            .document(authUser.uid)
            .collection("notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)
//          .limit(20)

        val snapshot = notificationsRef.get().await()
        val notificationList = snapshot.documents.mapNotNull {
            it.toObject(Notification::class.java)
        }
        _notifications.value = notificationList
    }

    override suspend fun deleteNotification(docId: String) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

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

    override suspend fun markRead(docId: String, read: Boolean) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val notificationRef = firestore
            .collection("users")
            .document(authUser.uid)
            .collection("notifications")

        val docRef = notificationRef.document(docId)
        docRef.update("read", read).await()

        _notifications.update { list ->
            list.map { if (it.docId == docId) it.copy(read = read) else it }
        }
    }

    override suspend fun markAllRead() {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val notificationsRef = firestore
            .collection("users")
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
        removeNotificationsListener()
        _notifications.value = emptyList()
    }
}

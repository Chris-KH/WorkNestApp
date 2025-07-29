package com.apcs.worknestapp.data.remote.notification

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Notification(
    @DocumentId val docId: String? = null,
    val title: String? = null,
    val message: String? = null,
    val read: Boolean? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
)

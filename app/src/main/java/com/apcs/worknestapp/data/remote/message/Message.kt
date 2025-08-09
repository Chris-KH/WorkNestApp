package com.apcs.worknestapp.data.remote.message

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Message(
    @DocumentId val docId: String? = null,
    val senderId: String? = null,
    val receiverId: String? = null,
    val text: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
)

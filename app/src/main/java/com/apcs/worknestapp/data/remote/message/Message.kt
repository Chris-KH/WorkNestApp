package com.apcs.worknestapp.data.remote.message

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ServerTimestamp

data class Message(
    @DocumentId val docId: String? = null,
    val sender: DocumentReference? = null,
    val receiver: DocumentReference? = null,
    val type: String? = null, // text, image, video
    val content: String? = null, // text or link to resource
    val deleteBy: DocumentReference? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
)

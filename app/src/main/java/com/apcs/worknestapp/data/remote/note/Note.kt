package com.apcs.worknestapp.data.remote.note

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Note(
    @DocumentId val docId: String? = null,
    val name: String? = null,
    val cover: String? = null,
    val description: String? = null,
    val completed: Boolean? = null,
    val archived: Boolean? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
)

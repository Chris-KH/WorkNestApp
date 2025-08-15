package com.apcs.worknestapp.data.remote.user

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference

data class Friendship(
    @DocumentId val docId: String? = null,
    val users: List<String>? = null,
    val sender: DocumentReference? = null,
    val receiver: DocumentReference? = null,
    val status: String? = null,
)

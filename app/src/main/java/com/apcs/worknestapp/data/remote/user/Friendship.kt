package com.apcs.worknestapp.data.remote.user

import com.google.firebase.firestore.DocumentId

data class Friendship(
    @DocumentId val docId: String? = null,
    val users: List<String>? = null,
    val senderId: String? = null,
    val receiverId: String? = null,
    val status: String? = null,
)

package com.apcs.worknestapp.data.remote.message

import com.apcs.worknestapp.data.remote.user.User
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference

data class Conservation(
    @DocumentId val docId: String? = null,
    val users: List<User>? = null,
    val lastMessage: DocumentReference? = null,
)

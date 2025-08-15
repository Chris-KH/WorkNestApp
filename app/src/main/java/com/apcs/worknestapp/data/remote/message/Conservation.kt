package com.apcs.worknestapp.data.remote.message

import com.apcs.worknestapp.data.remote.user.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference

data class Conservation(
    @DocumentId val docId: String? = null,
    val users: List<String>? = null,
    val sender: DocumentReference? = null,
    val lastContent: String? = null,
    val lastTime: Timestamp? = null,
    val seen: Boolean? = null,
)

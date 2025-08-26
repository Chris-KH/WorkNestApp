package com.apcs.worknestapp.data.remote.message

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference

data class Conservation(
    @DocumentId val docId: String? = null,
    val userIds: List<String>? = null,
    val sender: DocumentReference? = null,
    val lastContent: String? = null,
    val lastTime: Timestamp? = null,
    val seen: Boolean? = null,

    //Get user
    val userData: ConservationUserData = ConservationUserData(),
)

data class ConservationUserData(
    val name: String? = null,
    val avatar: String? = null,
    val online: Boolean? = null,
)

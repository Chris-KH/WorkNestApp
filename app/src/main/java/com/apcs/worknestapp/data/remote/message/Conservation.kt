package com.apcs.worknestapp.data.remote.message

import com.apcs.worknestapp.domain.usecase.AppDefault
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

    //Additional data
    val userData: ConservationUserData = ConservationUserData(),
    val messages: List<Message> = emptyList(),
)

data class ConservationUserData(
    @DocumentId val docId: String? = null,
    val name: String? = AppDefault.USER_NAME,
    val avatar: String? = AppDefault.AVATAR,
    val online: Boolean? = null,
)

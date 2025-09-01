package com.apcs.worknestapp.data.remote.message

import com.apcs.worknestapp.domain.usecase.AppDefault
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Exclude

data class Conservation(
    @DocumentId val docId: String? = null,
    val userIds: List<String>? = null,
    val sender: DocumentReference? = null,
    val lastContent: String? = null,
    val lastTime: Timestamp? = null,
    val senderSeen: Boolean? = null,
    val receiverSeen: Boolean? = null,

    //Additional data
    @get:Exclude val isTemporary: Boolean = false,
    @get:Exclude val userData: ConservationUserData = ConservationUserData(),
    @get:Exclude val messages: List<Message> = emptyList(),
)

data class ConservationUserData(
    @DocumentId val docId: String? = null,
    val name: String? = AppDefault.USER_NAME,
    val avatar: String? = AppDefault.AVATAR,
    val online: Boolean? = null,
)

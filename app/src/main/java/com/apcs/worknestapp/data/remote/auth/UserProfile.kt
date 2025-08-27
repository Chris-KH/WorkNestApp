package com.apcs.worknestapp.data.remote.auth

import com.apcs.worknestapp.domain.usecase.AppDefault
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class UserProfile(
    @DocumentId val docId: String? = null,
    val name: String? = null,
    val email: String? = null,
    val avatar: String? = AppDefault.AVATAR,
    val phone: String? = null,
    val address: String? = null,
    val bio: String? = null,
    val pronouns: String? = null,
    val online: Boolean? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
)

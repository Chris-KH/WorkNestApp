package com.apcs.worknestapp.data.remote.auth

import com.google.firebase.firestore.DocumentId
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    @DocumentId val docId: String? = null,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
)

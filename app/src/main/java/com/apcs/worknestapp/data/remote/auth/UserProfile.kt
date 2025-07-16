package com.apcs.worknestapp.data.remote.auth

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class UserProfile(
    @DocumentId val docId: String? = null,
    val name: String? = null,
    val email: String? = null,
    val avatar: String? = "https://res.cloudinary.com/dgniomynr/image/upload/v1749539388/profile-default-icon_rslcqz.png",
    val phone: String? = null,
    val address: String? = null,
    val bio: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
)

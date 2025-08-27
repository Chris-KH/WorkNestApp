package com.apcs.worknestapp.data.remote.user

import android.os.Parcelable
import com.apcs.worknestapp.domain.usecase.AppDefault
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    @DocumentId val docId: String? = null,
    val name: String? = null,
    val email: String? = null,
    val avatar: String? = AppDefault.AVATAR,
    val bio: String? = null,
    val pronouns: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
) : Parcelable

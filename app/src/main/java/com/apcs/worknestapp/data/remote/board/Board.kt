package com.apcs.worknestapp.data.remote.board

import com.apcs.worknestapp.data.remote.note.Note
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ServerTimestamp

data class Board(
    @DocumentId val docId: String? = null,
    val cover: Int? = null,
    val name: String? = null,
    val isLoading: Boolean? = null,
    val ownerId: String? = null,
    val memberIds: List<String> = emptyList(),
)

data class Notelist(
    @DocumentId val docId: String? = null,
    val name: String? = null,
    val cover: Int? = null,
    val archived: Boolean? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
)

data class ChecklistBoard(
    @DocumentId val docId: String? = null,
    val name: String? = null,
    val completed: Boolean? = null,
    val ownerId: String? = null,
)

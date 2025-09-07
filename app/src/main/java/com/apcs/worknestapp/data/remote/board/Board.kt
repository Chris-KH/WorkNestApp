package com.apcs.worknestapp.data.remote.board

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.apcs.worknestapp.data.remote.note.Note
import com.apcs.worknestapp.data.remote.user.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp

data class Board(
    @DocumentId val docId: String? = null,
    val name: String? = "Untitled Board",
    val cover: Int? = Color.Gray.toArgb(),
    val showNoteCover: Boolean? = null,
    val showCompletedStatus: Boolean? = null,
    val ownerId: String? = null,
    val memberIds: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,

    @get:Exclude val noteLists: List<NoteList> = emptyList(),
    @get:Exclude val members: List<User> = emptyList(),
    @get:Exclude val isLoading: Boolean? = null,
)

data class NoteList(
    @DocumentId val docId: String? = null,
    val name: String? = "New list",
    val archived: Boolean? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,

    @get:Exclude val notes: List<Note> = emptyList(),
)

data class ChecklistBoard(
    @DocumentId val docId: String? = null,
    val name: String? = null,
    val completed: Boolean? = null,
    val ownerId: String? = null,
)

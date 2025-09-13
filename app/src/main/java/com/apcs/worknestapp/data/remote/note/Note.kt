package com.apcs.worknestapp.data.remote.note

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp

data class Note(
    @DocumentId val docId: String? = null,
    val name: String? = null,
    val cover: Int? = null,
    val description: String? = null,
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null,
    val completed: Boolean? = false,
    val archived: Boolean? = false,
    val archivedByList: Boolean? = false,
    @ServerTimestamp val createdAt: Timestamp? = null,

    //Use-case, not fields of document Note
    @get:Exclude val checklists: List<Checklist> = emptyList(),
    @get:Exclude val comments: List<Comment> = emptyList(),
    @get:Exclude val isLoading: Boolean? = null,
)

data class Checklist(
    @DocumentId val docId: String? = null,
    val name: String? = "Checklist",

    //Use-case, not fields of document Checklists
    @get:Exclude val tasks: List<Task> = emptyList(),
)

data class Task(
    @DocumentId val docId: String? = null,
    val name: String? = "Task",
    val done: Boolean? = false,
)

data class Comment(
    @DocumentId val docId: String? = null,
    val content: String? = null,
    val createdBy: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
)

package com.apcs.worknestapp.data.remote.note

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Note(
    @DocumentId val docId: String? = null,
    val name: String? = null,
    val cover: Int? = null,
    val description: String? = null,
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null,
    val completed: Boolean? = null,
    val archived: Boolean? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,

    //Use-case, not fields of document Note
    val checklists: List<Checklist> = emptyList(),
    val comments: List<String> = emptyList(),
    val isLoading: Boolean? = null,
)

data class Checklist(
    @DocumentId val docId: String? = null,
    val name: String? = null,

    //Use-case, not fields of document Checklists
    val tasks: List<Task> = emptyList(),
)

data class Task(
    @DocumentId val docId: String? = null,
    val name: String? = null,
    val done: Boolean? = null,
)

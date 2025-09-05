package com.apcs.worknestapp.data.remote.board

import com.apcs.worknestapp.data.remote.note.Note
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow


interface BoardRepository {
    val boards: StateFlow<List<Board>>
    val notelists: StateFlow<List<Notelist>>
    val notes: StateFlow<List<Note>>

    fun removeBoardListener()
    fun registerBoardListener()
    suspend fun refreshBoard()
    suspend fun getBoard(docId: String): Board
    suspend fun addBoard(name: String, cover: Int?)
    suspend fun deleteBoard(docId: String): Boolean
    suspend fun deleteAllBoards()
    suspend fun updateBoardName(docId: String, name: String)
    suspend fun updateBoardCover(docId: String, color: Int?)
    suspend fun addMemberToBoard(boardId: String, userIdToAdd: String): Boolean
    suspend fun removeMemberFromBoard(boardId: String, userIdToRemove: String): Boolean

    suspend fun addNotelist(boardId: String, notelist: Notelist)
    suspend fun addNoteToList(boardId: String, notelistId: String, note: Note)
    suspend fun removeNotelist(boardId: String, notelistId: String)
    fun getNotelistsForBoard(boardId: String?): Flow<List<Notelist>>
    suspend fun getNotelist(boardId: String, notelistId: String): Notelist?

    suspend fun refreshNotelists(boardId: String)
    suspend fun updateNotelistName(boardId: String, notelistId: String, newName: String): Boolean
    fun registerNotelistListener(boardId: String)
    suspend fun updateNoteCheckedStatus(
        boardId: String,
        notelistId: String,
        noteId: String,
        isChecked: Boolean,
    ): Boolean

    fun removeNotelistListener()

    suspend fun getNote(boardId: String, notelistId: String, noteId: String): Note?
    fun removeNoteListener()
    fun registerNoteListener(boardId: String, notelistId: String)
    suspend fun refreshNotes(boardId: String, notelistId: String)
    suspend fun removeNoteFromNotelist(boardId: String, notelistId: String, noteId: String): Boolean
    suspend fun updateNoteName(
        boardId: String,
        notelistId: String,
        docId: String,
        name: String,
    ): Boolean

    suspend fun updateNoteCover(
        boardId: String,
        notelistId: String,
        docId: String,
        color: Int?,
    ): Boolean

    suspend fun updateNoteDescription(
        boardId: String,
        notelistId: String,
        docId: String,
        description: String,
    ): Boolean

    suspend fun updateNoteComplete(
        boardId: String,
        notelistId: String,
        docId: String,
        newState: Boolean,
    ): Boolean

    suspend fun updateNoteArchive(
        boardId: String,
        notelistId: String,
        docId: String,
        newState: Boolean,
    ): Boolean

    suspend fun updateNoteStartDate(
        boardId: String,
        notelistId: String,
        docId: String,
        dateTime: Timestamp?,
    ): Boolean

    suspend fun updateNoteEndDate(
        boardId: String,
        notelistId: String,
        docId: String,
        dateTime: Timestamp?,
    ): Boolean

    suspend fun addNewChecklistBoard(boardId: String, notelistId: String, noteId: String): Boolean
    suspend fun updateChecklistBoardName(
        boardId: String,
        notelistId: String,
        noteId: String,
        checklistId: String?,
        newName: String,
    ): Boolean

    suspend fun deleteChecklistBoard(
        boardId: String,
        notelistId: String,
        noteId: String,
        checklistId: String?,
    ): Boolean

    fun clearCache()
    suspend fun getChecklist(
        boardId: String,
        notelistId: String,
        noteId: String,
        checklistId: String,
    ): ChecklistBoard?

    fun getChecklists(
        boardId: String,
        notelistId: String,
        noteId: String,
    ): Flow<List<ChecklistBoard>>

    fun getNoteForNotelist(boardId: String, notelistId: String): Flow<List<Note>>
}

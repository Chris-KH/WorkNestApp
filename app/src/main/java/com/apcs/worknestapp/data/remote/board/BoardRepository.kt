package com.apcs.worknestapp.data.remote.board

import com.apcs.worknestapp.data.remote.note.Note
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow


interface BoardRepository {
    val boards: StateFlow<List<Board>>
    val noteLists: StateFlow<List<NoteList>>
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

    suspend fun addNoteList(boardId: String, noteList: NoteList)
    suspend fun addNoteToList(boardId: String, noteListId: String, note: Note)
    suspend fun removeNoteList(boardId: String, noteListId: String)
    fun getNoteListsForBoard(boardId: String?): Flow<List<NoteList>>
    suspend fun getNoteList(boardId: String, noteListId: String): NoteList?

    suspend fun refreshNoteLists(boardId: String)
    suspend fun updateNoteListName(boardId: String, noteListId: String, newName: String): Boolean
    fun registerNoteListListener(boardId: String)
    suspend fun updateNoteCheckedStatus(
        boardId: String,
        noteListId: String,
        noteId: String,
        isChecked: Boolean,
    ): Boolean

    fun removeNoteListListener()

    suspend fun getNote(boardId: String, noteListId: String, noteId: String): Note?
    fun removeNoteListener()
    fun registerNoteListener(boardId: String, noteListId: String)
    suspend fun refreshNotes(boardId: String, noteListId: String)
    suspend fun removeNoteFromNoteList(boardId: String, noteListId: String, noteId: String): Boolean
    suspend fun updateNoteName(
        boardId: String,
        noteListId: String,
        docId: String,
        name: String,
    ): Boolean

    suspend fun updateNoteCover(
        boardId: String,
        noteListId: String,
        docId: String,
        color: Int?,
    ): Boolean

    suspend fun updateNoteDescription(
        boardId: String,
        noteListId: String,
        docId: String,
        description: String,
    ): Boolean

    suspend fun updateNoteComplete(
        boardId: String,
        noteListId: String,
        docId: String,
        newState: Boolean,
    ): Boolean

    suspend fun updateNoteArchive(
        boardId: String,
        noteListId: String,
        docId: String,
        newState: Boolean,
    ): Boolean

    suspend fun updateNoteStartDate(
        boardId: String,
        noteListId: String,
        docId: String,
        dateTime: Timestamp?,
    ): Boolean

    suspend fun updateNoteEndDate(
        boardId: String,
        noteListId: String,
        docId: String,
        dateTime: Timestamp?,
    ): Boolean

    suspend fun addNewChecklistBoard(boardId: String, noteListId: String, noteId: String): Boolean
    suspend fun updateChecklistBoardName(
        boardId: String,
        noteListId: String,
        noteId: String,
        checklistId: String?,
        newName: String,
    ): Boolean

    suspend fun deleteChecklistBoard(
        boardId: String,
        noteListId: String,
        noteId: String,
        checklistId: String?,
    ): Boolean

    fun clearCache()
    suspend fun getChecklist(
        boardId: String,
        noteListId: String,
        noteId: String,
        checklistId: String,
    ): ChecklistBoard?

    fun getChecklists(
        boardId: String,
        noteListId: String,
        noteId: String,
    ): Flow<List<ChecklistBoard>>

    fun getNoteForNoteList(boardId: String, noteListId: String): Flow<List<Note>>
}

package com.apcs.worknestapp.data.remote.board

import com.apcs.worknestapp.data.remote.note.Note
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow


interface BoardRepository {
    val board: StateFlow<List<Board>>

    fun removeListener()
    fun registerListener()
    suspend fun refreshBoard()
    suspend fun getBoard(docId: String): Board
    suspend fun addBoard(name: String, cover: Int?)
    suspend fun deleteBoard(docId: String)
    suspend fun deleteAllBoards()
    suspend fun updateBoardName(docId: String, name: String)
    suspend fun updateBoardCover(docId: String, color: Int?)
    suspend fun addNotelist(boardId: String, notelist: Notelist)
    suspend fun addNoteToList(notelistId: String, note: Note)
    suspend fun removeNotelist(notelistId: String)
    suspend fun removeNoteFromNotelist(notelistId: String, noteId: String)
    suspend fun addMemberToBoard(boardId: String, userIdToAdd: String): Boolean
    suspend fun removeMemberFromBoard(boardId: String, userIdToRemove: String): Boolean
    fun getNotelistsForBoard(boardId: String?): Flow<List<Notelist>>
    suspend fun refreshNotelists(boardId: String)
    fun registerNotelistListener(boardId: String)
    fun removeNotelistListener()
    fun clearCache()
}
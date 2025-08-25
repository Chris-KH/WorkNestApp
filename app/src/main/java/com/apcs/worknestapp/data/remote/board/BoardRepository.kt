package com.apcs.worknestapp.data.remote.board

import com.apcs.worknestapp.data.remote.note.Note
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.StateFlow


interface BoardRepository {
    val board: StateFlow<List<Board>>

    fun removeListener()
    fun registerListener()
    suspend fun refreshBoard()
    suspend fun getBoard(docId: String): Board
    suspend fun addBoard(board: Board)
    suspend fun deleteBoard(docId: String)
    suspend fun deleteAllBoards()
    suspend fun updateBoardName(docId: String, name: String)
    suspend fun updateBoardCover(docId: String, color: Int?)
    suspend fun addNote(docId: String, note: Note)
    suspend fun removeNoteFromBoard(docId: String, noteId: String)
    suspend fun addMember()
    fun clearCache()
}
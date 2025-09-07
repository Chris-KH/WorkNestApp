package com.apcs.worknestapp.data.remote.board

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apcs.worknestapp.data.remote.note.Note
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BoardViewModel @Inject constructor(
    private val boardRepo: BoardRepository,
) : ViewModel() {
    val boards = boardRepo.boards
    val currentBoard = boardRepo.currentBoard

    fun removeBoardListener() {
        boardRepo.removeBoardListener()
    }

    fun registerBoardListener() {
        boardRepo.registerBoardListener()
    }

    fun removeNoteListListener() {
        boardRepo.removeNoteListListener()
    }

    fun registerNoteListListener(boardId: String) {
        boardRepo.registerNoteListListener(boardId)
    }

    fun registerNoteListener(boardId: String, noteListId: String) {
        boardRepo.registerNoteListener(boardId, noteListId)
    }

    fun removeNoteListener() {
        boardRepo.removeNoteListener()
    }

    suspend fun refreshBoardsIfEmpty(): Boolean {
        if (boards.value.isEmpty()) return refreshBoards()
        return true
    }

    suspend fun refreshBoards(): Boolean {
        return try {
            boardRepo.refreshBoard()
            true
        } catch(e: Exception) {
            Log.e("BoardViewModel", "Refresh boards failed", e)
            false
        }
    }

    fun createBoard(board: Board = Board()): Boolean {
        return try {
            boardRepo.addBoard(board)
            true
        } catch(e: Exception) {
            Log.e("BoardViewModel", "Create a board failed", e)
            false
        }
    }

    suspend fun getBoard(docId: String): Board? {
        return try {
            boardRepo.getBoard(docId)
        } catch(e: Exception) {
            Log.e("BoardViewModel", "Get board failed", e)
            null
        }
    }

    fun deleteBoard(docId: String): Boolean {
        return try {
            boardRepo.deleteBoard(docId)
            true
        } catch(e: Exception) {
            Log.e("BoardViewModel", "Delete board failed", e)
            false
        }
    }

    fun deleteAllBoards(): Boolean {
        return try {
            boardRepo.deleteAllBoards()
            true
        } catch(e: Exception) {
            Log.e("BoardViewModel", "Delete all boards failed", e)
            false
        }
    }

    suspend fun updateBoardName(docId: String, name: String): String? {
        return try {
            boardRepo.updateBoardName(docId, name)
            null
        } catch(e: Exception) {
            val message = "Update board name failed"
            Log.e("BoardViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun updateBoardDescription(docId: String, description: String): String? {
        return try {
            boardRepo.updateBoardDescription(docId, description)
            null
        } catch(e: Exception) {
            val message = "Update board description failed"
            Log.e("BoardViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun updateBoardShowNoteCover(docId: String, showNoteCover: Boolean): String? {
        return try {
            boardRepo.updateBoardShowNoteCover(docId, showNoteCover)
            null
        } catch(e: Exception) {
            val message = "Update board show note cover failed"
            Log.e("BoardViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun updateBoardShowCompletedStatus(
        docId: String,
        showCompletedStatus: Boolean,
    ): String? {
        return try {
            boardRepo.updateBoardShowCompletedStatus(docId, showCompletedStatus)
            null
        } catch(e: Exception) {
            val message = "Update board show completed status failed"
            Log.e("BoardViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun updateBoardCover(docId: String, color: Int?): String? {
        return try {
            boardRepo.updateBoardCover(docId, color)
            null
        } catch(e: Exception) {
            val message = "Update board cover failed"
            Log.e("BoardViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun addMemberToBoard(boardId: String, userIdToAdd: String): String? {
        return try {
            boardRepo.addMemberToBoard(boardId, userIdToAdd)
            null
        } catch(e: Exception) {
            val message = "Add member failed"
            Log.e("BoardViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun removeMemberFromBoard(boardId: String, userIdToRemove: String): String? {
        return try {
            boardRepo.removeMemberFromBoard(boardId, userIdToRemove)
            null
        } catch(e: Exception) {
            val message = "Remove member failed"
            Log.e("BoardViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun addNoteList(boardId: String, noteList: NoteList): String? {
        return try {
            boardRepo.addNoteList(boardId, noteList)
            null
        } catch(e: Exception) {
            val message = "Add note list failed"
            Log.e("BoardViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun removeNoteList(boardId: String, noteListId: String): String? {
        return try {
            boardRepo.removeNoteList(boardId, noteListId)
            null
        } catch(e: Exception) {
            val message = "Remove note list failed"
            Log.e("BoardViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun updateNoteListName(boardId: String, noteListId: String, newName: String): String? {
        return try {
            boardRepo.updateNoteListName(boardId, noteListId, newName)
            null
        } catch(e: Exception) {
            val message = "Update note list name failed"
            Log.e("BoardViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun addNoteToList(boardId: String, noteListId: String, note: Note): String? {
        return try {
            boardRepo.addNoteToList(boardId, noteListId, note)
            null
        } catch(e: Exception) {
            val message = "Add note failed"
            Log.e("BoardViewModel", message, e)
            e.message ?: message
        }
    }

    fun removeNoteFromNoteList(boardId: String, noteListId: String, noteId: String): Boolean {
        var deleted: Boolean = false
        viewModelScope.launch {
            boardRepo.removeNoteFromList(boardId, noteListId, noteId)
            deleted = true
        }
        return deleted
    }


    fun refreshNotes(boardId: String, noteListId: String) {
        viewModelScope.launch {
            boardRepo.refreshNotes(boardId, noteListId)
        }
    }


    fun updateNoteName(boardId: String, noteListId: String, docId: String, name: String): Boolean {
        var success = false
        viewModelScope.launch {
            if (boardRepo.updateNoteName(boardId, noteListId, docId, name))
                success = true
        }
        return success
    }

    fun updateNoteCover(boardId: String, noteListId: String, docId: String, color: Int?): Boolean {
        viewModelScope.launch {
            boardRepo.updateNoteCover(boardId, noteListId, docId, color)
        }
        return true
    }

    fun updateNoteDescription(
        boardId: String, noteListId: String, docId: String, description: String,
    ): Boolean {
        viewModelScope.launch {
            boardRepo.updateNoteDescription(boardId, noteListId, docId, description)
        }
        return true
    }

    fun updateNoteComplete(
        boardId: String,
        noteListId: String,
        docId: String,
        newState: Boolean,
    ): Boolean {
        var success = false
        viewModelScope.launch {
            if (boardRepo.updateNoteComplete(boardId, noteListId, docId, newState))
                success = true
        }
        return success
    }

    fun updateNoteArchive(
        boardId: String,
        noteListId: String,
        docId: String,
        newState: Boolean,
    ): Boolean {
        var success = false
        viewModelScope.launch {
            if (boardRepo.updateNoteArchive(boardId, noteListId, docId, newState))
                success = true
        }
        return success
    }

    fun updateNoteStartDate(
        boardId: String,
        noteListId: String,
        docId: String,
        dateTime: Timestamp?,
    ): Boolean {
        var success = false
        viewModelScope.launch {
            if (boardRepo.updateNoteStartDate(boardId, noteListId, docId, dateTime))
                success = true
        }
        return success
    }

    fun updateNoteEndDate(
        boardId: String,
        noteListId: String,
        docId: String,
        dateTime: Timestamp?,
    ): Boolean {
        var success = false

        viewModelScope.launch {
            if (boardRepo.updateNoteEndDate(boardId, noteListId, docId, dateTime))
                success = true
        }
        return success
    }

    fun updateNoteCheckedStatus(
        boardId: String,
        noteListId: String,
        noteId: String,
        isChecked: Boolean,
    ): Boolean {
        return false
    }

    fun getChecklist(boardId: String, noteListId: String, noteId: String, checklistId: String) {

    }

    fun addNewChecklist(boardId: String, noteListId: String, noteId: String): Boolean {
        var success = false
        viewModelScope.launch {
            if (boardRepo.addNewChecklistBoard(boardId, noteListId, noteId))
                success = true
        }
        return success
    }

    fun updateChecklistName(
        boardId: String,
        noteListId: String,
        noteId: String,
        checklistId: String?,
        newName: String,
    ): Boolean {
        var success = false
        viewModelScope.launch {
            if (boardRepo.updateChecklistBoardName(
                    boardId,
                    noteListId,
                    noteId,
                    checklistId,
                    newName
                )
            )
                success = true
        }
        return success
    }

    fun removeChecklist(
        boardId: String,
        noteListId: String,
        noteId: String,
        checklistId: String?,
    ): Boolean {
        var success = false
        viewModelScope.launch {
            if (boardRepo.deleteChecklistBoard(boardId, noteListId, noteId, checklistId))
                success = true
        }
        return success
    }

    fun clearCache() {
        boardRepo.clearCache()
    }
}

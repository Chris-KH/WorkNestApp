package com.apcs.worknestapp.data.remote.board

import android.util.Log
import androidx.lifecycle.ViewModel
import com.apcs.worknestapp.data.remote.note.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BoardViewModel @Inject constructor(
    private val boardRepo: BoardRepository,
) : ViewModel() {

    val boards = boardRepo.board

    fun removeListener() {
        boardRepo.removeListener()
    }

    fun registerListener() {
        try {
            boardRepo.registerListener()
        } catch (e: Exception) {
            Log.e("BoardViewModel", "Register listener for boards failed", e)
        }
    }

    suspend fun refreshBoardsIfEmpty(): Boolean {
        if (boards.value.isEmpty()) return refreshBoards()
        return true
    }

    suspend fun refreshBoards(): Boolean {
        return try {
            boardRepo.refreshBoard()
            true
        } catch (e: Exception) {
            Log.e("BoardViewModel", "Refresh boards failed", e)
            false
        }
    }

    suspend fun addBoard(board: Board): Boolean {
        return try {
            boardRepo.addBoard(board)
            true
        } catch (e: Exception) {
            Log.e("BoardViewModel", "Add a board failed", e)
            false
        }
    }

    suspend fun deleteBoard(docId: String): Boolean {
        return try {
            boardRepo.deleteBoard(docId)
            true
        } catch (e: Exception) {
            Log.e("BoardViewModel", "Delete board $docId failed", e)
            false
        }
    }

    suspend fun deleteAllBoards(): Boolean {
        return try {
            boardRepo.deleteAllBoards()
            true
        } catch (e: Exception) {
            Log.e("BoardViewModel", "Delete all boards failed", e)
            false
        }
    }

    suspend fun getBoard(docId: String): Board? {
        return try {
            boardRepo.getBoard(docId)
        } catch (e: Exception) {
            Log.e("BoardViewModel", "Get board $docId failed", e)
            null
        }
    }

    suspend fun updateBoardName(docId: String, name: String): Boolean {
        return try {
            boardRepo.updateBoardName(docId, name)
            true
        } catch (e: Exception) {
            Log.e("BoardViewModel", "Update board name failed", e)
            false
        }
    }

    suspend fun updateBoardCover(docId: String, color: Int?): Boolean {
        return try {
            boardRepo.updateBoardCover(docId, color)
            true
        } catch (e: Exception) {
            Log.e("BoardViewModel", "Update board cover failed", e)
            false
        }
    }

    suspend fun addNote(docId: String, note: Note): Boolean {
        return try {
            boardRepo.addNote(docId, note)
            true
        } catch (e: Exception) {
            Log.e("BoardViewModel", "Add note to board $docId failed", e)
            false
        }
    }

    suspend fun removeNoteFromBoard(docId: String, noteId: String): Boolean {
        return try {
            boardRepo.removeNoteFromBoard(docId, noteId)
            true
        } catch (e: Exception) {
            Log.e("BoardViewModel", "Remove note $noteId from board $docId failed", e)
            false
        }
    }

    suspend fun addMemberToBoard(boardId: String, userIdToAdd: String): Boolean {
        return try {
            (boardRepo as? BoardRepositoryImpl)?.addMemberToBoard(boardId, userIdToAdd)
            true
        } catch (e: Exception) {
            Log.e("BoardViewModel", "Add member $userIdToAdd to board $boardId failed", e)
            false
        }
    }

    suspend fun removeMemberFromBoard(boardId: String, userIdToRemove: String): Boolean {
        return try {
            (boardRepo as? BoardRepositoryImpl)?.removeMemberFromBoard(boardId, userIdToRemove)
            true
        } catch (e: Exception) {
        Log.e("BoardViewModel", "Remove member $userIdToRemove from board $boardId failed", e)
        false
    }
}


fun clearCache() {
        boardRepo.clearCache()
    }
}

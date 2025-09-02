package com.apcs.worknestapp.data.remote.board

import kotlinx.coroutines.launch
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apcs.worknestapp.data.remote.note.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    fun registerNotelistListener(boardId: String?) {
        if (boardId == null) {
            Log.w("BoardViewModel", "Board ID is null, cannot register notelist listener.")
            return
        }
        try {
            boardRepo.registerNotelistListener(boardId)
        } catch (e: Exception) {
            Log.e("BoardViewModel", "Register listener for notelists failed", e)
        }
    }

    fun removeNotelistListener() {
        boardRepo.removeNotelistListener()
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

    fun createBoard(name: String, cover: Int?) {
        viewModelScope.launch {
            try {
                boardRepo.addBoard(name, cover)
                Log.d("BoardViewModel", "Board '$name' creation process initiated successfully via repository.")
            } catch (e: Exception) {
                Log.e("BoardViewModel", "Error creating board '$name'", e)
            }
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
    fun updateBoardName(boardId: String, newName: String) {
        viewModelScope.launch {
            try {
                boardRepo.updateBoardName(boardId, newName)
            } catch (e: SecurityException) {
                Log.e("YourViewModel", "Permission error updating board name: ${e.message}")
            } catch (e: Exception) {
                Log.e("YourViewModel", "Error updating board name: ${e.message}")
            }
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

    suspend fun addNotelist(boardId: String, notelist: Notelist): Boolean {
        return try {
            boardRepo.addNotelist(boardId, notelist)
            true
        } catch (e: Exception) {
            Log.e("BoardViewModel", "Add notelist failed", e)
            false
        }
    }

    suspend fun addNoteToList(notelistId: String, note: Note): Boolean {
        return try {
            boardRepo.addNoteToList(notelistId, note)
            true
        } catch (e: Exception) {
            Log.e("BoardViewModel", "Add note to notelist $notelistId failed", e)
            false
        }
    }

    suspend fun removeNotelist(notelistId: String): Boolean {
        return try {
            boardRepo.removeNotelist(notelistId)
            true
        } catch (e: Exception) {
            Log.e("BoardViewModel", "Remove notelist $notelistId failed", e)
            false
        }
    }

    suspend fun removeNoteFromNotelist(notelistId: String, noteId: String): Boolean {
        return try {
            boardRepo.removeNoteFromNotelist(notelistId, noteId)
            true
        } catch (e: Exception) {
            Log.e("BoardViewModel", "Remove note $noteId from notelist $notelistId failed", e)
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
    fun getNotelistsForBoard(boardId: String?): Flow<List<Notelist>> {
        return boardRepo.getNotelistsForBoard(boardId)
    }

    suspend fun refreshNotelists(boardId: String): Boolean {
        return try {
            boardRepo.refreshNotelists(boardId)
            true
        } catch (e: Exception) {
            Log.e("BoardViewModel", "Refresh notelists failed", e)
            false
        }
    }

    fun updateNotelistName(boardId: String, notelistId: String, newName: String) {
        viewModelScope.launch {
            try {
                boardRepo.updateNotelistName(boardId, notelistId, newName)
            } catch (e: SecurityException) {
                Log.e("BoardViewModel", "Rename notelists failed", e)
                false
            }
        }
    }
    fun updateUserNoteCheckedStatus(boardId: String, notelistId: String, noteId: String, isChecked: Boolean) {
        viewModelScope.launch {
            val success = boardRepo.updateNoteCheckedStatus(boardId, notelistId, noteId, isChecked)
            if (success) {
                Log.d("ViewModel", "Note checked status updated.")
            } else {
                Log.e("ViewModel", "Failed to update note checked status.")
            }
        }
    }


    fun clearCache() {
        boardRepo.clearCache()
    }
}
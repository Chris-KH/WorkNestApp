package com.apcs.worknestapp.data.remote.board

import kotlinx.coroutines.launch
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apcs.worknestapp.data.remote.note.Note
import com.google.firebase.Timestamp
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
    val notelistsForBoard = boardRepo.notelists
    val notes = boardRepo.notes

    fun removeBoardListener() {
        boardRepo.removeBoardListener()
    }

    fun registerBoardListener() {
        viewModelScope.launch {
            boardRepo.registerBoardListener()
        }
    }

    fun removeNotelistListener() {
        boardRepo.removeNotelistListener()
    }

    fun registerNotelistListener(boardId: String?) {
        if (boardId == null) {
            return
        }
        viewModelScope.launch {
            boardRepo.registerNotelistListener(boardId)
        }
    }

    fun removeNoteListener() {
        boardRepo.removeNoteListener()
    }

    fun registerNoteListener(boardId: String, notelistId: String) {
        viewModelScope.launch {
            boardRepo.registerNoteListener(boardId, notelistId)
        }
    }

    fun refreshBoards() {
        viewModelScope.launch {
            boardRepo.refreshBoard()
        }
    }

    fun createBoard(name: String, cover: Int?) {
        viewModelScope.launch {
            boardRepo.addBoard(name, cover)
        }
    }

    fun deleteBoard(docId: String) : Boolean {
        var deleted : Boolean = false
        viewModelScope.launch {
            if (boardRepo.deleteBoard(docId))
            deleted = true
        }
        return deleted
    }

    fun deleteAllBoards() {
        viewModelScope.launch {
            boardRepo.deleteAllBoards()
        }
    }

    fun getBoard(docId: String) {
        viewModelScope.launch {
            boardRepo.getBoard(docId)
        }
    }

    fun updateBoardName(boardId: String, newName: String) {
        viewModelScope.launch {
            boardRepo.updateBoardName(boardId, newName)
        }
    }

    fun updateBoardCover(docId: String, color: Int?) {
        viewModelScope.launch {
            boardRepo.updateBoardCover(docId, color)
        }
    }

    fun addNotelist(boardId: String, notelist: Notelist) {
        viewModelScope.launch {
            boardRepo.addNotelist(boardId, notelist)
        }
    }

    fun addNoteToList(boardId: String, notelistId: String, note: Note) {
        viewModelScope.launch {
            boardRepo.addNoteToList(boardId, notelistId, note)
        }
    }

    fun removeNotelist(boardId: String, notelistId: String) {
        viewModelScope.launch {
            boardRepo.removeNotelist(boardId, notelistId)
        }
    }

    fun removeNoteFromNotelist(boardId: String, notelistId: String, noteId: String) {
        viewModelScope.launch {
            boardRepo.removeNoteFromNotelist(boardId, notelistId, noteId)
        }
    }

    fun addMemberToBoard(boardId: String, userIdToAdd: String) {
        viewModelScope.launch {
            (boardRepo as? BoardRepositoryImpl)?.addMemberToBoard(boardId, userIdToAdd)
        }
    }

    fun removeMemberFromBoard(boardId: String, userIdToRemove: String) {
        viewModelScope.launch {
            (boardRepo as? BoardRepositoryImpl)?.removeMemberFromBoard(boardId, userIdToRemove)
        }
    }

    fun getNotelistsForBoard(boardId: String?): Flow<List<Notelist>> {
        return boardRepo.getNotelistsForBoard(boardId)
    }

    fun refreshNotelists(boardId: String) {
        viewModelScope.launch {
            boardRepo.refreshNotelists(boardId)
        }
    }

    fun refreshNotes(boardId: String, notelistId: String) {
        viewModelScope.launch {
            boardRepo.refreshNotes(boardId, notelistId)
        }
    }

    fun updateNotelistName(boardId: String, notelistId: String, newName: String) {
        viewModelScope.launch {
            boardRepo.updateNotelistName(boardId, notelistId, newName)
        }
    }

    fun updateNoteName(boardId: String, notelistId: String, docId: String, name: String) {
        viewModelScope.launch {
            boardRepo.updateNoteName(boardId, notelistId, docId, name)
        }
    }

    fun updateNoteCover(boardId: String, notelistId: String, docId: String, color: Int?) {
        viewModelScope.launch {
            boardRepo.updateNoteCover(boardId, notelistId, docId, color)
        }
    }

    fun updateNoteDescription(boardId: String, notelistId: String, docId: String, description: String) {
        viewModelScope.launch {
            boardRepo.updateNoteDescription(boardId, notelistId, docId, description)
        }
    }

    fun updateNoteComplete(boardId: String, notelistId: String, docId: String, newState: Boolean) {
        viewModelScope.launch {
            boardRepo.updateNoteComplete(boardId, notelistId, docId, newState)
        }
    }

    fun updateNoteArchive(boardId: String, notelistId: String, docId: String, newState: Boolean) {
        viewModelScope.launch {
            boardRepo.updateNoteArchive(boardId, notelistId, docId, newState)
        }
    }

    fun updateNoteStartDate(boardId: String, notelistId: String, docId: String, dateTime: Timestamp?) {
        viewModelScope.launch {
            boardRepo.updateNoteStartDate(boardId, notelistId, docId, dateTime)
        }
    }

    fun updateNoteEndDate(boardId: String, notelistId: String, docId: String, dateTime: Timestamp?) {
        viewModelScope.launch {
            boardRepo.updateNoteEndDate(boardId, notelistId, docId, dateTime)
        }
    }

    fun updateNoteCheckedStatus(boardId: String, notelistId: String, noteId: String, isChecked: Boolean) {
        viewModelScope.launch {
            boardRepo.updateNoteCheckedStatus(boardId, notelistId, noteId, isChecked)
        }
    }

    fun clearCache() {
        boardRepo.clearCache()
    }
}


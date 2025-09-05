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
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class BoardViewModel @Inject constructor(
    private val boardRepo: BoardRepository,
) : ViewModel() {
    val boards = boardRepo.boards
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes = _notes.asStateFlow()

    private val _selectedNote = MutableStateFlow<Note?>(null)
    val selectedNote: StateFlow<Note?> = _selectedNote.asStateFlow()

    private val _currentChecklist = MutableStateFlow<ChecklistBoard?>(null)
    val currentChecklist: StateFlow<ChecklistBoard?> = _currentChecklist

    private val _checklists = MutableStateFlow<List<ChecklistBoard>>(emptyList())
    val checklists: StateFlow<List<ChecklistBoard>> = _checklists

    fun getChecklists(boardId: String, notelistId: String, noteId: String) {
        viewModelScope.launch {
            try {
                (boardRepo as? BoardRepositoryImpl)?.getChecklists(boardId, notelistId, noteId)
                    ?.collect { checklistList ->
                        _checklists.value = checklistList
                    }
            } catch(e: Exception) {
                Log.e("BoardViewModel", "Error fetching checklists: ${e.message}")
                _checklists.value = emptyList()
            }
        }
    }

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

    fun deleteBoard(docId: String): Boolean {
        var deleted: Boolean = false
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

    fun removeNoteFromNotelist(boardId: String, notelistId: String, noteId: String): Boolean {
        var deleted: Boolean = false
        viewModelScope.launch {
            boardRepo.removeNoteFromNotelist(boardId, notelistId, noteId)
            deleted = true
        }
        return deleted
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

//    fun getNotelist(boardId: String, notelistId: String) {
//        viewModelScope.launch {
//            boardRepo.getNotelist(boardId, notelistId)
//        }
//    }


    fun getNotesForNotelist(boardId: String, notelistId: String) {
        viewModelScope.launch {
            try {
                boardRepo.getNoteForNotelist(boardId, notelistId)
                    .collect { notesList ->
                        _notes.value = notesList
                    }
            } catch(e: Exception) {
                // This will catch any exceptions not handled by the Flow itself.
                // The repository handles the PERMISSION_DENIED case.
                Log.e("BoardViewModel", "Error fetching notes: ${e.message}")
                _notes.value = emptyList() // Or handle the error appropriately
            }
        }
    }

    fun getNote(boardId: String, notelistId: String, noteId: String) {
        viewModelScope.launch {
            val note = boardRepo.getNote(boardId, notelistId, noteId)
            _selectedNote.value = note
        }
    }

    fun updateNoteName(boardId: String, notelistId: String, docId: String, name: String): Boolean {
        var success = false
        viewModelScope.launch {
            if (boardRepo.updateNoteName(boardId, notelistId, docId, name))
                success = true
        }
        return success
    }

    fun updateNoteCover(boardId: String, notelistId: String, docId: String, color: Int?): Boolean {
        viewModelScope.launch {
            boardRepo.updateNoteCover(boardId, notelistId, docId, color)
        }
        return true
    }

    fun updateNoteDescription(
        boardId: String,
        notelistId: String,
        docId: String,
        description: String,
    ): Boolean {
        viewModelScope.launch {
            boardRepo.updateNoteDescription(boardId, notelistId, docId, description)
        }
        return true
    }

    fun updateNoteComplete(
        boardId: String,
        notelistId: String,
        docId: String,
        newState: Boolean,
    ): Boolean {
        var success = false
        viewModelScope.launch {
            if (boardRepo.updateNoteComplete(boardId, notelistId, docId, newState))
                success = true
        }
        return success
    }

    fun updateNoteArchive(
        boardId: String,
        notelistId: String,
        docId: String,
        newState: Boolean,
    ): Boolean {
        var success = false
        viewModelScope.launch {
            if (boardRepo.updateNoteArchive(boardId, notelistId, docId, newState))
                success = true
        }
        return success
    }

    fun updateNoteStartDate(
        boardId: String,
        notelistId: String,
        docId: String,
        dateTime: Timestamp?,
    ): Boolean {
        var success = false
        viewModelScope.launch {
            if (boardRepo.updateNoteStartDate(boardId, notelistId, docId, dateTime))
                success = true
        }
        return success
    }

    fun updateNoteEndDate(
        boardId: String,
        notelistId: String,
        docId: String,
        dateTime: Timestamp?,
    ): Boolean {
        var success = false

        viewModelScope.launch {
            if (boardRepo.updateNoteEndDate(boardId, notelistId, docId, dateTime))
                success = true
        }
        return success
    }

    fun updateNoteCheckedStatus(
        boardId: String,
        notelistId: String,
        noteId: String,
        isChecked: Boolean,
    ): Boolean {
        var success = false
        viewModelScope.launch {
            if (boardRepo.updateNoteCheckedStatus(boardId, notelistId, noteId, isChecked))
                success = true
        }
        return success
    }

    fun getChecklist(boardId: String, notelistId: String, noteId: String, checklistId: String) {
        viewModelScope.launch {
            try {
                val checklist = (boardRepo as? BoardRepositoryImpl)?.getChecklist(
                    boardId,
                    notelistId,
                    noteId,
                    checklistId
                )
                _currentChecklist.value = checklist
            } catch(e: Exception) {
                Log.e("BoardViewModel", "Error fetching checklist: ${e.message}")
                _currentChecklist.value = null
            }
        }
    }

    fun addNewChecklist(boardId: String, notelistId: String, noteId: String): Boolean {
        var success = false
        viewModelScope.launch {
            if (boardRepo.addNewChecklistBoard(boardId, notelistId, noteId))
                success = true
        }
        return success
    }

    fun updateChecklistName(
        boardId: String,
        notelistId: String,
        noteId: String,
        checklistId: String?,
        newName: String,
    ): Boolean {
        var success = false
        viewModelScope.launch {
            if (boardRepo.updateChecklistBoardName(
                    boardId,
                    notelistId,
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
        notelistId: String,
        noteId: String,
        checklistId: String?,
    ): Boolean {
        var success = false
        viewModelScope.launch {
            if (boardRepo.deleteChecklistBoard(boardId, notelistId, noteId, checklistId))
                success = true
        }
        return success
    }

    fun clearCache() {
        boardRepo.clearCache()
    }
}

package com.apcs.worknestapp.data.remote.board

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
import kotlinx.coroutines.launch
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

    fun getChecklists(boardId: String, noteListId: String, noteId: String) {
        viewModelScope.launch {
            try {
                (boardRepo as? BoardRepositoryImpl)?.getChecklists(boardId, noteListId, noteId)
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

    fun removeNoteListListener() {
        boardRepo.removeNoteListListener()
    }

    fun registerNoteListListener(boardId: String?) {
        if (boardId == null) {
            return
        }
        viewModelScope.launch {
            boardRepo.registerNoteListListener(boardId)
        }
    }

    fun removeNoteListener() {
        boardRepo.removeNoteListener()
    }

    fun registerNoteListener(boardId: String, noteListId: String) {
        viewModelScope.launch {
            boardRepo.registerNoteListener(boardId, noteListId)
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

    fun addNoteList(boardId: String, noteList: NoteList) {
        viewModelScope.launch {
            boardRepo.addNoteList(boardId, noteList)
        }
    }

    fun addNoteToList(boardId: String, noteListId: String, note: Note) {
        viewModelScope.launch {
            boardRepo.addNoteToList(boardId, noteListId, note)
        }
    }

    fun removeNoteList(boardId: String, noteListId: String) {
        viewModelScope.launch {
            boardRepo.removeNoteList(boardId, noteListId)
        }
    }

    fun removeNoteFromNoteList(boardId: String, noteListId: String, noteId: String): Boolean {
        var deleted: Boolean = false
        viewModelScope.launch {
            boardRepo.removeNoteFromNoteList(boardId, noteListId, noteId)
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

    fun getNoteListsForBoard(boardId: String?): Flow<List<NoteList>> {
        return boardRepo.getNoteListsForBoard(boardId)
    }

    fun refreshNoteLists(boardId: String) {
        viewModelScope.launch {
            boardRepo.refreshNoteLists(boardId)
        }
    }

    fun refreshNotes(boardId: String, noteListId: String) {
        viewModelScope.launch {
            boardRepo.refreshNotes(boardId, noteListId)
        }
    }

    fun updateNoteListName(boardId: String, noteListId: String, newName: String) {
        viewModelScope.launch {
            boardRepo.updateNoteListName(boardId, noteListId, newName)
        }
    }

//    fun getNoteList(boardId: String, noteListId: String) {
//        viewModelScope.launch {
//            boardRepo.getNoteList(boardId, noteListId)
//        }
//    }


    fun getNotesForNoteList(boardId: String, noteListId: String) {
        viewModelScope.launch {
            try {
                boardRepo.getNoteForNoteList(boardId, noteListId)
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

    fun getNote(boardId: String, noteListId: String, noteId: String) {
        viewModelScope.launch {
            val note = boardRepo.getNote(boardId, noteListId, noteId)
            _selectedNote.value = note
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
        boardId: String,
        noteListId: String,
        docId: String,
        description: String,
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
        var success = false
        viewModelScope.launch {
            if (boardRepo.updateNoteCheckedStatus(boardId, noteListId, noteId, isChecked))
                success = true
        }
        return success
    }

    fun getChecklist(boardId: String, noteListId: String, noteId: String, checklistId: String) {
        viewModelScope.launch {
            try {
                val checklist = (boardRepo as? BoardRepositoryImpl)?.getChecklist(
                    boardId,
                    noteListId,
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

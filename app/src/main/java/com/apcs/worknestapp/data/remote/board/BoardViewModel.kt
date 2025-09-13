package com.apcs.worknestapp.data.remote.board

import android.util.Log
import androidx.lifecycle.ViewModel
import com.apcs.worknestapp.data.remote.note.Checklist
import com.apcs.worknestapp.data.remote.note.Comment
import com.apcs.worknestapp.data.remote.note.Note
import com.apcs.worknestapp.data.remote.note.Task
import com.apcs.worknestapp.data.remote.user.User
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BoardViewModel @Inject constructor(
    private val boardRepo: BoardRepository,
) : ViewModel() {
    val boards = boardRepo.boards
    val currentBoard = boardRepo.currentBoard
    val currentNote = boardRepo.currentNote

    fun registerBoardsListener() = boardRepo.registerBoardsListener()
    fun removeBoardsListener() = boardRepo.removeBoardsListener()

    fun registerCurrentBoardListener(boardId: String) =
        boardRepo.registerCurrentBoardListener(boardId)

    fun removeCurrentBoardListener() = boardRepo.removeCurrentBoardListener()

    fun registerCurrentNoteListener(boardId: String, noteListId: String, noteId: String) {
        boardRepo.registerCurrentNoteListener(boardId, noteListId, noteId)
    }

    fun removeCurrentNoteListener() {
        boardRepo.removeCurrentNoteListener()
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

    fun deleteBoard(docId: String): String? {
        return try {
            boardRepo.deleteBoard(docId)
            null
        } catch(e: Exception) {
            val message = "Delete board failed"
            Log.e("BoardViewModel", message, e)
            e.message ?: message
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

    suspend fun addMemberToBoard(boardId: String, user: User): String? {
        return try {
            boardRepo.addMemberToBoard(boardId, user)
            null
        } catch(e: Exception) {
            val message = "Add member failed"
            Log.e("BoardViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun removeMemberFromBoard(boardId: String, user: User): String? {
        return try {
            boardRepo.removeMemberFromBoard(boardId, user)
            null
        } catch(e: Exception) {
            val message = "Remove member failed"
            Log.e("BoardViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun leaveBoard(boardId: String): String? {
        return try {
            boardRepo.leaveBoard(boardId)
            null
        } catch(e: Exception) {
            val message = "Leave bỏard failed"
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

    suspend fun updateNoteListArchive(
        boardId: String,
        noteListId: String,
        newState: Boolean,
    ): String? {
        return try {
            boardRepo.updateNoteListArchive(boardId, noteListId, newState)
            null
        } catch(e: Exception) {
            val message = "${if (newState) "Archive" else "Unarchived"} note list failed"
            Log.e("BoardViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun addNoteToList(boardId: String, noteListId: String, note: Note): String? {
        return try {
            boardRepo.addNoteToNoteList(boardId, noteListId, note)
            null
        } catch(e: Exception) {
            val message = "Add note failed"
            Log.e("BoardViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun removeNoteFromNoteList(
        boardId: String,
        noteListId: String,
        noteId: String,
    ): String? {
        return try {
            boardRepo.removeNoteFromNoteList(boardId, noteListId, noteId)
            null
        } catch(e: Exception) {
            val message = "Delete note failed"
            Log.e("BoardViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun archiveCompletedNotesInList(boardId: String, noteListId: String): String? {
        return try {
            boardRepo.archiveCompletedNotesInList(boardId, noteListId)
            null
        } catch(e: Exception) {
            val message = "Archive completed in this list notes failed"
            Log.e("BoardViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun archiveAllNotesInList(boardId: String, noteListId: String): String? {
        return try {
            boardRepo.archiveAllNotesInList(boardId, noteListId)
            null
        } catch(e: Exception) {
            val message = "Archive all notes in this list failed"
            Log.e("BoardViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun deleteAllNotesInList(boardId: String, noteListId: String): String? {
        return try {
            boardRepo.deleteAllNotesInList(boardId, noteListId)
            null
        } catch(e: Exception) {
            val message = "Delete all notes in this list failed"
            Log.e("BoardViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun getNote(boardId: String, noteListId: String, noteId: String): Note? {
        return try {
            boardRepo.getNote(boardId, noteListId, noteId)
        } catch(e: Exception) {
            Log.e("BoardViewModel", e.message, e)
            null
        }
    }


    suspend fun updateNoteName(
        boardId: String,
        noteListId: String,
        noteId: String,
        name: String,
    ): String? {
        return try {
            boardRepo.updateNoteName(boardId, noteListId, noteId, name)
            null
        } catch(e: Exception) {
            val message = "Update note name failed"
            Log.e("NoteViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun updateNoteCover(
        boardId: String,
        noteListId: String,
        noteId: String,
        color: Int?,
    ): String? {
        return try {
            boardRepo.updateNoteCover(boardId, noteListId, noteId, color)
            null
        } catch(e: Exception) {
            val message = "Update note cover failed"
            Log.e("NoteViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun updateNoteDescription(
        boardId: String,
        noteListId: String,
        noteId: String,
        description: String,
    ): String? {
        return try {
            boardRepo.updateNoteDescription(boardId, noteListId, noteId, description)
            null
        } catch(e: Exception) {
            val message = "Update note description failed"
            Log.e("NoteViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun updateNoteComplete(
        boardId: String,
        noteListId: String,
        noteId: String,
        newState: Boolean,
    ): String? {
        return try {
            boardRepo.updateNoteComplete(boardId, noteListId, noteId, newState)
            null
        } catch(e: Exception) {
            val message = "Update note complete failed"
            Log.e("NoteViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun updateNoteArchive(
        boardId: String,
        noteListId: String,
        noteId: String,
        newState: Boolean,
    ): String? {
        return try {
            boardRepo.updateNoteArchive(boardId, noteListId, noteId, newState)
            null
        } catch(e: Exception) {
            val message = "Update note archive failed"
            Log.e("NoteViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun updateNoteStartDate(
        boardId: String,
        noteListId: String,
        noteId: String,
        dateTime: Timestamp?,
    ): String? {
        return try {
            boardRepo.updateNoteStartDate(boardId, noteListId, noteId, dateTime)
            null
        } catch(e: Exception) {
            val message = "Update note start date failed"
            Log.e("NoteViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun updateNoteEndDate(
        boardId: String,
        noteListId: String,
        noteId: String,
        dateTime: Timestamp?,
    ): String? {
        return try {
            boardRepo.updateNoteEndDate(boardId, noteListId, noteId, dateTime)
            null
        } catch(e: Exception) {
            val message = "Update note end date failed"
            Log.e("NoteViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun addNewChecklist(
        boardId: String,
        noteListId: String,
        noteId: String, checklist: Checklist,
    ): String? {
        return try {
            boardRepo.addNewChecklist(boardId, noteListId, noteId, checklist)
            null
        } catch(e: Exception) {
            val message = "Add new checklist failed"
            Log.e("NoteViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun deleteChecklist(
        boardId: String,
        noteListId: String,
        noteId: String, checklistId: String,
    ): String? {
        return try {
            boardRepo.deleteChecklist(boardId, noteListId, noteId, checklistId)
            null
        } catch(e: Exception) {
            val message = "Delete checklist failed"
            Log.e("NoteViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun updateChecklistName(
        boardId: String,
        noteListId: String,
        noteId: String, checklistId: String, name: String,
    ): String? {
        return try {
            boardRepo.updateChecklistName(boardId, noteListId, noteId, checklistId, name)
            null
        } catch(e: Exception) {
            val message = "Update checklist name failed"
            Log.e("NoteViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun addNewTask(
        boardId: String,
        noteListId: String,
        noteId: String, checklistId: String, task: Task,
    ): String? {
        return try {
            boardRepo.addNewTask(boardId, noteListId, noteId, checklistId, task)
            null
        } catch(e: Exception) {
            val message = "Add new task failed"
            Log.e("NoteViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun deleteTask(
        boardId: String,
        noteListId: String,
        noteId: String, checklistId: String, taskId: String,
    ): String? {
        return try {
            boardRepo.deleteTask(boardId, noteListId, noteId, checklistId, taskId)
            null
        } catch(e: Exception) {
            val message = "Delete task failed"
            Log.e("NoteViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun updateTaskName(
        boardId: String,
        noteListId: String,
        noteId: String, checklistId: String, taskId: String, name: String,
    ): String? {
        return try {
            boardRepo.updateTaskName(boardId, noteListId, noteId, checklistId, taskId, name)
            null
        } catch(e: Exception) {
            val message = "Update task name failed"
            Log.e("NoteViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun updateTaskDone(
        boardId: String,
        noteListId: String,
        noteId: String, checklistId: String, taskId: String, done: Boolean,
    ): String? {
        return try {
            boardRepo.updateTaskDone(boardId, noteListId, noteId, checklistId, taskId, done)
            null
        } catch(e: Exception) {
            val message = "Update task done to $done failed"
            Log.e("NoteViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun addComment(
        boardId: String,
        noteListId: String,
        noteId: String, comment: Comment,
    ): String? {
        return try {
            boardRepo.addComment(boardId, noteListId, noteId, comment)
            null
        } catch(e: Exception) {
            val message = "Add comment to note failed"
            Log.e("NoteViewModel", message, e)
            e.message ?: message
        }
    }

    suspend fun deleteComment(
        boardId: String,
        noteListId: String,
        noteId: String, commentId: String,
    ): String? {
        return try {
            boardRepo.deleteComment(boardId, noteListId, noteId, commentId)
            null
        } catch(e: Exception) {
            val message = "Delete comment in note failed"
            Log.e("NoteViewModel", message, e)
            e.message ?: message
        }
    }

    fun clearCache() {
        boardRepo.clearCache()
    }
}

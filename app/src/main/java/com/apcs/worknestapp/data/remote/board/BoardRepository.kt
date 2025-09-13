package com.apcs.worknestapp.data.remote.board

import com.apcs.worknestapp.data.remote.note.Checklist
import com.apcs.worknestapp.data.remote.note.Comment
import com.apcs.worknestapp.data.remote.note.Note
import com.apcs.worknestapp.data.remote.note.Task
import com.apcs.worknestapp.data.remote.user.User
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.StateFlow


interface BoardRepository {
    val boards: StateFlow<List<Board>>
    val currentBoard: StateFlow<Board?>
    val currentNote: StateFlow<Note?>

    //Listener
    fun registerBoardsListener()
    fun removeBoardsListener()
    fun registerCurrentBoardListener(boardId: String)
    fun removeCurrentBoardListener()
    fun registerCurrentNoteListener(boardId: String, noteListId: String, noteId: String)
    fun removeCurrentNoteListener()

    fun addBoard(board: Board)
    fun deleteBoard(docId: String)
    suspend fun refreshBoard()
    suspend fun getBoard(docId: String): Board
    suspend fun updateBoardName(docId: String, name: String)
    suspend fun updateBoardDescription(docId: String, description: String)
    suspend fun updateBoardShowNoteCover(docId: String, showNoteCover: Boolean)
    suspend fun updateBoardShowCompletedStatus(docId: String, showCompletedStatus: Boolean)
    suspend fun updateBoardCover(docId: String, color: Int?)
    suspend fun addMemberToBoard(boardId: String, user: User)
    suspend fun removeMemberFromBoard(boardId: String, user: User)
    suspend fun leaveBoard(boardId: String)

    suspend fun addNoteList(boardId: String, noteList: NoteList)
    suspend fun removeNoteList(boardId: String, noteListId: String)
    suspend fun updateNoteListName(boardId: String, noteListId: String, name: String)
    suspend fun updateNoteListArchive(boardId: String, noteListId: String, newState: Boolean)
    suspend fun addNoteToNoteList(boardId: String, noteListId: String, note: Note)
    suspend fun removeNoteFromNoteList(boardId: String, noteListId: String, noteId: String)
    suspend fun archiveCompletedNotesInList(boardId: String, noteListId: String)
    suspend fun archiveAllNotesInList(boardId: String, noteListId: String)
    suspend fun deleteAllNotesInList(boardId: String, noteListId: String)

    suspend fun getNote(boardId: String, noteListId: String, noteId: String): Note
    suspend fun updateNoteName(boardId: String, noteListId: String, noteId: String, name: String)
    suspend fun updateNoteCover(boardId: String, noteListId: String, noteId: String, color: Int?)
    suspend fun updateNoteDescription(
        boardId: String,
        noteListId: String,
        noteId: String,
        description: String,
    )

    suspend fun updateNoteComplete(
        boardId: String,
        noteListId: String,
        noteId: String,
        newState: Boolean,
    )

    suspend fun updateNoteArchive(
        boardId: String,
        noteListId: String,
        noteId: String,
        newState: Boolean,
    )

    suspend fun updateNoteStartDate(
        boardId: String,
        noteListId: String,
        noteId: String,
        dateTime: Timestamp?,
    )

    suspend fun updateNoteEndDate(
        boardId: String,
        noteListId: String,
        noteId: String,
        dateTime: Timestamp?,
    )

    suspend fun addNewChecklist(
        boardId: String,
        noteListId: String,
        noteId: String, checklist: Checklist,
    )

    suspend fun deleteChecklist(
        boardId: String,
        noteListId: String,
        noteId: String, checklistId: String,
    )

    suspend fun updateChecklistName(
        boardId: String,
        noteListId: String,
        noteId: String, checklistId: String, name: String,
    )

    suspend fun addNewTask(
        boardId: String,
        noteListId: String,
        noteId: String, checklistId: String, task: Task,
    )

    suspend fun deleteTask(
        boardId: String,
        noteListId: String,
        noteId: String, checklistId: String, taskId: String,
    )

    suspend fun updateTaskName(
        boardId: String,
        noteListId: String,
        noteId: String, checklistId: String, taskId: String, name: String,
    )

    suspend fun updateTaskDone(
        boardId: String,
        noteListId: String,
        noteId: String, checklistId: String, taskId: String, done: Boolean,
    )

    suspend fun addComment(
        boardId: String,
        noteListId: String,
        noteId: String, comment: Comment,
    )

    suspend fun deleteComment(
        boardId: String,
        noteListId: String,
        noteId: String, commentId: String,
    )

    fun clearCache()
}

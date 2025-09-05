package com.apcs.worknestapp.data.remote.board

import android.util.Log
import androidx.room.util.query
import com.apcs.worknestapp.data.remote.note.Note
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.collections.sortedByDescending

class BoardRepositoryImpl @Inject constructor() : BoardRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("BoardRepository", "Coroutine crashed", throwable)
    }
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + errorHandler)

    private var boardListener: ListenerRegistration? = null
    private var noteListListener: ListenerRegistration? = null
    private var notesListener: ListenerRegistration? = null

    private val _boards = MutableStateFlow<List<Board>>(emptyList())
    override val boards: StateFlow<List<Board>> = _boards.asStateFlow()

    private val _currentBoard = MutableStateFlow<Board?>(null)
    override val currentBoard: StateFlow<Board?> = _currentBoard.asStateFlow()

    private val _noteLists = MutableStateFlow<List<NoteList>>(emptyList())
    override val noteLists: StateFlow<List<NoteList>> = _noteLists.asStateFlow()

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    override val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    init {
        auth.addAuthStateListener {
            if (it.currentUser == null) {
                clearCache()
            }
        }
    }

    private fun boardNotFound(boardId: String) {
        if (_currentBoard.value?.docId == boardId) _currentBoard.value = null
        _boards.update { list -> list.filterNot { it.docId == boardId } }
    }

    //TODO: OK
    override fun registerBoardListener() {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val boardsRef = firestore.collection("boards")
            .where(
                Filter.or(
                    Filter.equalTo("ownerId", authUser.uid),
                    Filter.arrayContains("memberIds", authUser.uid)
                )
            )

        boardListener?.remove()
        boardListener = boardsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("BoardRepository", "Listen boards snapshot failed", error)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.metadata.isFromCache) {
                val remoteBoards = snapshot.documents.mapNotNull {
                    it.toObject(Board::class.java)
                }
                val pendingBoards = _boards.value.filter { local ->
                    local.isLoading == true && remoteBoards.none { it.docId == local.docId }
                }
                _boards.value = (remoteBoards + pendingBoards).sortedByDescending { it.createdAt }
            }
        }
    }

    //TODO: OK
    override fun removeBoardListener() {
        boardListener?.remove()
        boardListener = null
    }

    override fun registerNoteListener(boardId: String, noteListId: String) {
        val authUser = auth.currentUser ?: return

        removeNoteListener()

        val notesRef = firestore
            .collection("boards")
            .document(boardId)
            .collection("notelists")
            .document(noteListId)
            .collection("notes")
            .whereEqualTo("archived", false)

        notesListener = notesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                }
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val remoteNotes = snapshot.documents.mapNotNull {
                    it.toObject(Note::class.java)
                }
                _notes.value = remoteNotes
            }
        }
    }

    override fun removeNoteListener() {
        notesListener?.remove()
        notesListener = null
        _notes.value = emptyList()
    }

    override fun registerNoteListListener(boardId: String) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        removeNoteListListener()
        val noteListsRef = firestore.collection("boards").document(boardId).collection("notelists")
        noteListListener = noteListsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("BoardRepository", "Listen notelists snapshot failed", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val noteListList = snapshot.documents.mapNotNull {
                    it.toObject(NoteList::class.java)
                }
                _noteLists.value = noteListList
            }
        }
    }

    override fun removeNoteListListener() {
        noteListListener?.remove()
        noteListListener = null
        _noteLists.value = emptyList()
    }

    //TODO: OK
    override suspend fun refreshBoard() {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val boardsSnapshot = firestore.collection("boards")
            .where(
                Filter.or(
                    Filter.equalTo("ownerId", authUser.uid),
                    Filter.arrayContains("memberIds", authUser.uid)
                )
            )
            .get()
            .await()

        val boards = boardsSnapshot.documents.mapNotNull { it.toObject(Board::class.java) }
        _boards.value = boards.sortedByDescending { it.createdAt }
    }

    //TODO: OK
    override suspend fun getBoard(docId: String): Board {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val boardRef = firestore.collection("boards").document(docId)
        val boardDoc = boardRef.get().await()
        if (!boardDoc.exists()) throw Exception("Board not found")

        val board = boardDoc.toObject(Board::class.java) ?: throw Exception("Invalid board format")
        if (board.ownerId != authUser.uid && !board.memberIds.contains(authUser.uid)) {
            throw SecurityException("User does not have access to this board.")
        }

        val noteListsSnapshot = boardRef.collection("notelists").get().await()
        val noteLists = coroutineScope {
            noteListsSnapshot.documents.mapNotNull { noteListDoc ->
                async {
                    try {
                        val noteList = noteListDoc.toObject(NoteList::class.java)
                        val notesSnapshot = noteListDoc.reference.collection("notes").get().await()
                        val notes = notesSnapshot.documents.mapNotNull {
                            it.toObject(Note::class.java)
                        }
                        noteList?.copy(notes = notes)
                    } catch(_: Exception) {
                        null
                    }
                }
            }.awaitAll().filterNotNull()
        }

        val result = board.copy(noteLists = noteLists)
        _currentBoard.value = result
        return result
    }

    //TODO: OK
    override fun addBoard(board: Board) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        repoScope.launch {
            val boardRef = firestore.collection("boards").document()
            val boardId = boardRef.id

            val tempBoard = board.copy(
                docId = boardId,
                ownerId = authUser.uid,
                memberIds = listOf(authUser.uid),
                createdAt = Timestamp.now(),
            )
            withContext(Dispatchers.Main) {
                _boards.update { list ->
                    (list + tempBoard.copy(isLoading = true)).sortedByDescending { it.createdAt }
                }
            }

            try {
                boardRef.set(tempBoard).await()
                withContext(Dispatchers.Main) {
                    _boards.update { list ->
                        (list.filterNot { it.docId == boardId } + tempBoard.copy(isLoading = false))
                            .sortedByDescending { it.createdAt }
                    }
                }
            } catch(e: Exception) {
                withContext(Dispatchers.Main) {
                    _boards.update { list ->
                        list.filterNot { it.docId == boardId }.sortedByDescending { it.createdAt }
                    }
                }
                throw e
            }
        }
    }

    //TODO: OK
    override fun deleteBoard(docId: String) {
        auth.currentUser ?: throw Exception("User not logged in")

        repoScope.launch {
            firestore.collection("boards").document(docId).delete().await()

            withContext(Dispatchers.Main) {
                _boards.update { list -> list.filterNot { it.docId == docId } }

                if (_currentBoard.value?.docId == docId) _currentBoard.value = null
            }
        }
    }

    //TODO: OK
    override fun deleteAllBoards() {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val boardsToDeleteQuery = firestore.collection("boards")
            .whereEqualTo("ownerId", authUser.uid)

        val previousState = _boards.value
        _boards.update { list -> list.filterNot { it.ownerId == authUser.uid } }

        repoScope.launch {
            try {
                val snapshot = boardsToDeleteQuery.get().await()
                if (snapshot.isEmpty) return@launch

                val batch = firestore.batch()
                snapshot.documents.forEach { batch.delete(it.reference) }
                batch.commit().await()
            } catch(e: Exception) {
                withContext(Dispatchers.Main) {
                    _boards.value = previousState
                }
                throw e
            }
        }
    }

    //TODO: OK
    override suspend fun updateBoardName(docId: String, name: String) {
        auth.currentUser ?: throw IllegalStateException("User not logged in")
        val boardRef = firestore.collection("boards").document(docId)

        try {
            boardRef.update("name", name).await()
            _boards.update { list ->
                list.map { if (it.docId == docId) it.copy(name = name) else it }
            }
            if (_currentBoard.value?.docId == docId) {
                _currentBoard.update { it?.copy(name = name) }
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                boardNotFound(docId)
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    //TODO: OK
    override suspend fun updateBoardCover(docId: String, color: Int?) {
        auth.currentUser ?: throw Exception("User not logged in")
        val boardRef = firestore.collection("boards").document(docId)

        try {
            boardRef.update("cover", color).await()
            _boards.update { list ->
                list.map {
                    if (it.docId == docId) it.copy(cover = color) else it
                }
            }
            if (_currentBoard.value?.docId == docId) {
                _currentBoard.update { it?.copy(cover = color) }
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                boardNotFound(docId)
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    override suspend fun addMemberToBoard(boardId: String, userIdToAdd: String): Boolean {
        val currentUser = auth.currentUser ?: throw Exception("User not logged in")
        val boardRef = firestore.collection("boards").document(boardId)

        val boardSnapshot = boardRef.get().await()
        val board = boardSnapshot.toObject(Board::class.java)
        if (board == null || board.ownerId != currentUser.uid) {
            throw SecurityException("Only the board owner can add members.")
        }

        if (board.memberIds.contains(userIdToAdd)) {
            return true
        }

        boardRef.update("memberIds", FieldValue.arrayUnion(userIdToAdd)).await()
        _boards.update { list ->
            list.map {
                if (it.docId == boardId) it.copy(memberIds = it.memberIds + userIdToAdd) else it
            }
        }
        return true
    }

    override suspend fun removeMemberFromBoard(boardId: String, userIdToRemove: String): Boolean {
        val currentUser = auth.currentUser ?: throw Exception("User not logged in")
        val boardRef = firestore.collection("boards").document(boardId)

        val boardSnapshot = boardRef.get().await()
        val board = boardSnapshot.toObject(Board::class.java)
        if (board == null) throw Exception("Board not found")

        if (board.ownerId != currentUser.uid && userIdToRemove != currentUser.uid) {
            throw SecurityException("Only the board owner or the member themselves can perform this action.")
        }
        if (board.ownerId == userIdToRemove && board.ownerId == currentUser.uid) {
            Log.w(
                "BoardRepository",
                "Owner cannot remove themselves from the members list directly. Consider transferring ownership."
            )
        }


        boardRef.update("memberIds", FieldValue.arrayRemove(userIdToRemove)).await()
        _boards.update { list ->
            list.map {
                if (it.docId == boardId) it.copy(memberIds = it.memberIds - userIdToRemove) else it
            }
        }
        return true
    }

    /////////////TODO : Ignore this Just a separator

    override suspend fun addNoteList(boardId: String, noteList: NoteList) {
        val currentUser = auth.currentUser ?: throw Exception("User not logged in")
        val boardRef = firestore.collection("boards").document(boardId)

        val boardSnapshot = boardRef.get().await()
        val boardData = boardSnapshot.toObject(Board::class.java)
        if (boardData == null || (!boardData.memberIds.contains(currentUser.uid) && boardData.ownerId != currentUser.uid)) {
            throw SecurityException("User must be a member to add a notelist to this board.")
        }

        val notelistCollectionRef = boardRef.collection("notelists")

        val newNotelistWithId = noteList.copy()

        notelistCollectionRef.add(newNotelistWithId).await()
    }


    override suspend fun addNoteToList(boardId: String, noteListId: String, note: Note) {
        Log.d("BoardRepositoryImpl", "Adding note to list: $noteListId")
        val currentUser = auth.currentUser ?: throw Exception("User not logged in")

        val notesCollectionRef = firestore.collection("boards").document(boardId)
            .collection("notelists").document(noteListId)
            .collection("notes")

        val boardDoc = firestore.collection("boards").document(boardId).get().await()
        val boardData = boardDoc.toObject(Board::class.java)
        if (boardData == null || (!boardData.memberIds.contains(currentUser.uid) && boardData.ownerId != currentUser.uid)) {
            throw SecurityException("User must be a member or owner to add a note to this note list.")
        }

        notesCollectionRef.add(note).await()
    }

    override suspend fun removeNoteList(boardId: String, noteListId: String) {
        val currentUser = auth.currentUser ?: throw Exception("User not logged in")
        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)

        val boardSnapshot = boardRef.get().await()
        val board = boardSnapshot.toObject(Board::class.java)
        if (board == null || (!board.memberIds.contains(currentUser.uid) && board.ownerId != currentUser.uid)) {
            throw SecurityException("User must be a member or owner to remove this notelist.")
        }
        val notesSnapshot = noteListRef.collection("notes").get().await()
        for(noteDoc in notesSnapshot.documents) {
            noteDoc.reference.delete().await()
        }
        noteListRef.delete().await()
    }

    override suspend fun removeNoteFromNoteList(
        boardId: String,
        noteListId: String,
        noteId: String,
    ): Boolean {
        val currentUser = auth.currentUser ?: throw Exception("User not logged in")
        val noteRef = firestore.collection("boards").document(boardId)
            .collection("notelists").document(noteListId)
            .collection("notes").document(noteId)

        val boardDoc = firestore.collection("boards").document(boardId).get().await()
        val boardData = boardDoc.toObject(Board::class.java)
        if (boardData == null || (!boardData.memberIds.contains(currentUser.uid) && boardData.ownerId != currentUser.uid)) {
            throw SecurityException("User must be a member/owner to remove this note.")
            return false
        }

        noteRef.delete().await()
        return true
    }

    override suspend fun getNoteList(boardId: String, noteListId: String): NoteList? {
        val noteListRef = firestore.collection("boards").document(boardId)
            .collection("notelists").document(noteListId)

        return try {
            val noteListSnapshot = noteListRef.get().await()
            noteListSnapshot.toObject(NoteList::class.java)
        } catch(e: Exception) {
            if (e is com.google.firebase.firestore.FirebaseFirestoreException && e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                Log.e(
                    "BoardRepositoryImpl",
                    "PERMISSION_DENIED: User does not have access to this notelist.",
                    e
                )
                throw SecurityException("User does not have access to this notelist.")
            }
            Log.e("BoardRepositoryImpl", "Error getting notelist: ${e.message}", e)
            null
        }
    }

    override suspend fun refreshNoteLists(boardId: String) {
        val noteListsQuery =
            firestore.collection("boards").document(boardId).collection("notelists")
                .get()
                .await()
        val noteListList = noteListsQuery.documents.mapNotNull {
            it.toObject(NoteList::class.java)
        }
    }

    override fun getNoteListsForBoard(boardId: String?): Flow<List<NoteList>> {
        if (boardId == null) {
            return flowOf(emptyList())
        }

        return callbackFlow {
            // The Firestore SDK will perform the security check on the server.
            // We do not need a redundant client-side check for permissions.
            val noteListsQuery =
                firestore.collection("boards").document(boardId).collection("notelists")

            val listenerRegistration = noteListsQuery.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // If a permission error occurs, we handle it here.
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.e(
                            "BoardRepositoryImpl",
                            "PERMISSION_DENIED: User does not have access to this board's notelists."
                        )
                        // Send an empty list or a specific error state to the UI
                        trySend(emptyList())
                        // Optionally, close the flow to stop listening for updates
                        close(error)
                    } else {
                        // For other errors, close the flow normally
                        Log.e("BoardRepositoryImpl", "Error getting notelists: ${error.message}")
                        close(error)
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val noteListList = snapshot.documents.mapNotNull {
                        it.toObject(NoteList::class.java)
                    }
                    trySend(noteListList)
                }
            }
            awaitClose {
                listenerRegistration.remove()
            }
        }
    }


    override suspend fun updateNoteListName(
        boardId: String,
        noteListId: String,
        newName: String,
    ): Boolean {
        val currentUser = auth.currentUser ?: return false

        if (boardId.isBlank() || noteListId.isBlank() || newName.isBlank()) {
            return false
        }

        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)

        return try {
            val boardDoc = boardRef.get().await()
            val boardData = boardDoc.toObject(Board::class.java)

            if (boardData == null || (!boardData.memberIds.contains(currentUser.uid) && boardData.ownerId != currentUser.uid)) {
                return false
            }

            noteListRef.update("name", newName).await()
            true
        } catch(e: Exception) {
            false
        }
    }

    override suspend fun updateNoteCheckedStatus(
        boardId: String,
        noteListId: String,
        noteId: String,
        isChecked: Boolean,
    ): Boolean {
        val currentUser = auth.currentUser ?: return false

        if (boardId.isBlank() || noteListId.isBlank() || noteId.isBlank()) {
            return false
        }

        val boardRef = firestore.collection("boards").document(boardId)
        val noteRef = boardRef.collection("notelists").document(noteListId)
            .collection("notes").document(noteId)

        return try {
            val boardDoc = boardRef.get().await()
            val boardData = boardDoc.toObject(Board::class.java)

            if (boardData == null || (!boardData.memberIds.contains(currentUser.uid) && boardData.ownerId != currentUser.uid)) {
                return false
            }

            noteRef.update("completed", isChecked).await()
            true
        } catch(e: Exception) {
            false
        }
    }
    ///////////////TODO: IGNORE THIS

    override suspend fun getNote(boardId: String, noteListId: String, noteId: String): Note? {
        val noteRef = firestore.collection("boards").document(boardId)
            .collection("notelists").document(noteListId)
            .collection("notes").document(noteId)

        val currentUser = auth.currentUser ?: throw SecurityException("User not logged in.")
        val boardDoc = firestore.collection("boards").document(boardId).get().await()
        val boardData = boardDoc.toObject(Board::class.java)

        if (boardData == null || (!boardData.memberIds.contains(currentUser.uid) && boardData.ownerId != currentUser.uid)) {
            throw SecurityException("User does not have access to this note.")
        }

        return try {
            val noteSnapshot = noteRef.get().await()
            noteSnapshot.toObject(Note::class.java)
        } catch(e: Exception) {
            Log.e("BoardRepositoryImpl", "Error getting note: ${e.message}", e)
            null
        }
    }


    override suspend fun refreshNotes(boardId: String, noteListId: String) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val notesRef = firestore
            .collection("boards")
            .document(boardId)
            .collection("notelists")
            .document(noteListId)
            .collection("notes")

        val snapshot = notesRef.get().await()
        val noteList = snapshot.documents.mapNotNull {
            it.toObject(Note::class.java)
        }
        _notes.value = noteList
    }

    override suspend fun updateNoteName(
        boardId: String,
        noteListId: String,
        docId: String,
        name: String,
    ): Boolean {
        val currentUser = auth.currentUser ?: return false

        val noteRef = firestore.collection("boards").document(boardId)
            .collection("notelists").document(noteListId)
            .collection("notes").document(docId)

        return try {
            noteRef.update("name", name).await()
            true
        } catch(e: Exception) {
            false
        }
    }

    override suspend fun updateNoteCover(
        boardId: String,
        noteListId: String,
        docId: String,
        color: Int?,
    ): Boolean {
        val authUser = auth.currentUser ?: return false

        val noteRef = firestore.collection("boards").document(boardId)
            .collection("notelists").document(noteListId)
            .collection("notes").document(docId)

        return try {
            noteRef.update("cover", color).await()
            true
        } catch(e: Exception) {
            false
        }
    }

    override suspend fun updateNoteDescription(
        boardId: String,
        noteListId: String,
        docId: String,
        description: String,
    ): Boolean {
        val currentUser = auth.currentUser ?: return false

        val noteRef = firestore.collection("boards").document(boardId)
            .collection("notelists").document(noteListId)
            .collection("notes").document(docId)

        return try {
            noteRef.update("description", description).await()
            true
        } catch(e: Exception) {
            false
        }
    }

    override suspend fun updateNoteComplete(
        boardId: String,
        noteListId: String,
        docId: String,
        newState: Boolean,
    ): Boolean {
        val currentUser = auth.currentUser ?: return false

        val noteRef = firestore.collection("boards").document(boardId)
            .collection("notelists").document(noteListId)
            .collection("notes").document(docId)

        return try {
            noteRef.update("completed", newState).await()
            true
        } catch(e: Exception) {
            false
        }
    }

    override suspend fun updateNoteArchive(
        boardId: String,
        noteListId: String,
        docId: String,
        newState: Boolean,
    ): Boolean {
        val currentUser = auth.currentUser ?: return false

        val noteRef = firestore.collection("boards").document(boardId)
            .collection("notelists").document(noteListId)
            .collection("notes").document(docId)

        return try {
            noteRef.update("archived", newState).await()
            true
        } catch(e: Exception) {
            false
        }
    }

    override suspend fun updateNoteStartDate(
        boardId: String,
        noteListId: String,
        docId: String,
        dateTime: Timestamp?,
    ): Boolean {
        val currentUser = auth.currentUser ?: return false

        val noteRef = firestore.collection("boards").document(boardId)
            .collection("notelists").document(noteListId)
            .collection("notes").document(docId)

        return try {
            noteRef.update("startDate", dateTime).await()
            true
        } catch(e: Exception) {
            false
        }
    }

    override suspend fun addNewChecklistBoard(
        boardId: String,
        noteListId: String,
        noteId: String,
    ): Boolean {
        val currentUser = auth.currentUser ?: throw SecurityException("User not logged in")

        val boardRef = firestore.collection("boards").document(boardId)
        val noteRef = boardRef.collection("notelists").document(noteListId)
            .collection("notes").document(noteId)

        val checklistRef = noteRef.collection("checklists").document()
        val newChecklistBoard = ChecklistBoard(
            docId = checklistRef.id,
            name = "New Task",
            completed = false,
            ownerId = currentUser.uid
        )

        return try {
            firestore.runTransaction { transaction ->
                val boardSnapshot = transaction.get(boardRef)
                val boardData = boardSnapshot.toObject(Board::class.java)

                if (boardData == null || (!boardData.memberIds.contains(currentUser.uid) && boardData.ownerId != currentUser.uid)) {
                    throw SecurityException("User does not have permission to add a checklist to this note.")
                }
                transaction.set(checklistRef, newChecklistBoard)
            }.await()
            true
        } catch(e: Exception) {
            Log.e("BoardRepositoryImpl", "Failed to add new checklist board", e)
            false
        }
    }

    override suspend fun updateNoteEndDate(
        boardId: String,
        noteListId: String,
        docId: String,
        dateTime: Timestamp?,
    ): Boolean {
        val currentUser = auth.currentUser ?: return false

        val noteRef = firestore.collection("boards").document(boardId)
            .collection("notelists").document(noteListId)
            .collection("notes").document(docId)

        return try {
            noteRef.update("endDate", dateTime).await()
            true
        } catch(e: Exception) {
            false
        }
    }

    override suspend fun updateChecklistBoardName(
        boardId: String,
        noteListId: String,
        noteId: String,
        checklistId: String?,
        newName: String,
    ): Boolean {
        val currentUser = auth.currentUser ?: throw SecurityException("User not logged in")

        val boardRef = firestore.collection("boards").document(boardId)
        val checklistRef = boardRef.collection("notelists").document(noteListId)
            .collection("notes").document(noteId)
            .collection("checklists").document(checklistId!!)

        return try {
            val boardSnapshot = boardRef.get().await()
            val boardData = boardSnapshot.toObject(Board::class.java)
            if (boardData == null || (!boardData.memberIds.contains(currentUser.uid) && boardData.ownerId != currentUser.uid)) {
                throw SecurityException("User does not have permission to update this checklist board.")
            }
            checklistRef.update("name", newName).await()
            true
        } catch(e: Exception) {
            Log.e("BoardRepositoryImpl", "Failed to update checklist board name", e)
            false
        }
    }

    override suspend fun getChecklist(
        boardId: String,
        noteListId: String,
        noteId: String,
        checklistId: String,
    ): ChecklistBoard? {
        val currentUser = auth.currentUser ?: throw SecurityException("User not logged in")

        val checklistRef = firestore.collection("boards").document(boardId)
            .collection("notelists").document(noteListId)
            .collection("notes").document(noteId)
            .collection("checklists").document(checklistId)

        // Security check: Verify user access to the parent board.
        val boardDoc = firestore.collection("boards").document(boardId).get().await()
        val boardData = boardDoc.toObject(Board::class.java)
        if (boardData == null || (!boardData.memberIds.contains(currentUser.uid) && boardData.ownerId != currentUser.uid)) {
            throw SecurityException("User does not have access to this checklist.")
        }

        // Try to fetch the document and convert it to a ChecklistBoard object.
        return try {
            val checklistSnapshot = checklistRef.get().await()
            checklistSnapshot.toObject(ChecklistBoard::class.java)
        } catch(e: Exception) {
            Log.e("BoardRepositoryImpl", "Error getting checklist: ${e.message}", e)
            null
        }
    }

    override fun getChecklists(
        boardId: String,
        noteListId: String,
        noteId: String,
    ): Flow<List<ChecklistBoard>> {
        val currentUser = auth.currentUser ?: return flowOf(emptyList())

        return callbackFlow {
            val checklistCollectionRef = firestore.collection("boards").document(boardId)
                .collection("notelists").document(noteListId)
                .collection("notes").document(noteId)
                .collection("checklists")

            val boardDoc = firestore.collection("boards").document(boardId).get().await()
            val boardData = boardDoc.toObject(Board::class.java)

            if (boardData == null || (!boardData.memberIds.contains(currentUser.uid) && boardData.ownerId != currentUser.uid)) {
                close(SecurityException("User does not have access to this note's checklists."))
                return@callbackFlow
            }

            val listenerRegistration =
                checklistCollectionRef.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val checklistList = snapshot.documents.mapNotNull {
                            it.toObject(ChecklistBoard::class.java)
                        }
                        trySend(checklistList).isSuccess
                    }
                }

            awaitClose {
                listenerRegistration.remove()
            }
        }
    }

    override suspend fun deleteChecklistBoard(
        boardId: String,
        noteListId: String,
        noteId: String,
        checklistId: String?,
    ): Boolean {
        val currentUser = auth.currentUser ?: throw SecurityException("User not logged in")

        val boardRef = firestore.collection("boards").document(boardId)
        val checklistRef = boardRef.collection("notelists").document(noteListId)
            .collection("notes").document(noteId)
            .collection("checklists").document(checklistId!!)

        return try {
            val boardSnapshot = boardRef.get().await()
            val boardData = boardSnapshot.toObject(Board::class.java)
            if (boardData == null || (!boardData.memberIds.contains(currentUser.uid) && boardData.ownerId != currentUser.uid)) {
                throw SecurityException("User does not have permission to delete this checklist board.")
            }
            checklistRef.delete().await()
            true
        } catch(e: Exception) {
            Log.e("BoardRepositoryImpl", "Failed to delete checklist board", e)
            false
        }
    }

    override fun getNoteForNoteList(boardId: String, noteListId: String): Flow<List<Note>> {
        return callbackFlow {
            val notesQuery = firestore.collection("boards")
                .document(boardId)
                .collection("notelists")
                .document(noteListId)
                .collection("notes")
                .orderBy("createdAt", Query.Direction.ASCENDING) // Order notes by creation time

            val listenerRegistration = notesQuery.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.e(
                            "BoardRepositoryImpl",
                            "PERMISSION_DENIED: User does not have access to these notes."
                        )
                        trySend(emptyList())
                        close(error)
                    } else {
                        Log.e(
                            "BoardRepositoryImpl",
                            "Error getting notes for notelist: ${error.message}"
                        )
                        close(error)
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val noteList = snapshot.documents.mapNotNull {
                        it.toObject<Note>()
                    }
                    trySend(noteList)
                }
            }
            awaitClose {
                listenerRegistration.remove()
            }
        }
    }


    override fun clearCache() {
        repoScope.coroutineContext.cancelChildren()
        removeBoardListener()
        removeNoteListener()
        removeNoteListListener()
        _currentBoard.value = null
        _boards.value = emptyList()
    }
}

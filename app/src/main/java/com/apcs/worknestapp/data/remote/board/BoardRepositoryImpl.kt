package com.apcs.worknestapp.data.remote.board

import android.util.Log
import com.apcs.worknestapp.data.remote.note.Note
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BoardRepositoryImpl @Inject constructor() : BoardRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _boards = MutableStateFlow<List<Board>>(emptyList())
    override val board: StateFlow<List<Board>> = _boards

    private val _notelists = MutableStateFlow<List<Notelist>>(emptyList())
    val notelists: StateFlow<List<Notelist>> = _notelists
    private var boardListener: ListenerRegistration? = null
    private var notelistListener: ListenerRegistration? = null


    init {
        auth.addAuthStateListener {
            if (it.currentUser == null) {
                removeListener()
                _boards.value = emptyList()}
        }
    }

    override fun removeListener() {
        boardListener?.remove()
        boardListener = null
    }

    override fun registerListener() {
        val currentUser = auth.currentUser ?: throw Exception("User not logged in")
        val userId = currentUser.uid


        val ownedBoardsQuery = firestore.collection("boards")
            .whereEqualTo("ownerId", userId)

        val memberBoardsQuery = firestore.collection("boards")
            .whereArrayContains("memberIds", userId)

        boardListener?.remove()

        val boardsRef = firestore.collection("boards")
            .whereArrayContains("memberIds", userId)

        boardListener = boardsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("BoardRepository", "Listen boards snapshot failed", error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val boardList = snapshot.documents.mapNotNull {
                    it.toObject(Board::class.java)
                }
                _boards.value = boardList
            }
        }
    }

    override suspend fun refreshBoard() {
        val currentUser = auth.currentUser ?: throw Exception("User not logged in")
        val userId = currentUser.uid

        val ownedBoardsQuery = firestore.collection("boards")
            .whereEqualTo("ownerId", userId)
            .get()
            .await()

        val memberBoardsQuery = firestore.collection("boards")
            .whereArrayContains("memberIds", userId)
            .whereNotEqualTo("ownerId", userId)
            .get()
            .await()

        val ownedBoards = ownedBoardsQuery.documents.mapNotNull { it.toObject(Board::class.java) }
        val memberBoards = memberBoardsQuery.documents.mapNotNull { it.toObject(Board::class.java) }

        _boards.value = (ownedBoards + memberBoards).distinctBy { it.docId }
    }
    override suspend fun getBoard(docId: String): Board {
        val boardDoc = firestore.collection("boards")
            .document(docId)
            .get()
            .await()

        val board = boardDoc.toObject(Board::class.java) ?: throw Exception("Invalid board format or board not found")

        val currentUser = auth.currentUser ?: throw Exception("User not logged in")
        if (board.ownerId != currentUser.uid && !board.memberIds.contains(currentUser.uid)) {
            throw SecurityException("User does not have access to this board.")
        }
        return board
    }

    override suspend fun addBoard(name: String, cover: Int?) {
        val currentUser = auth.currentUser ?: throw Exception("User not logged in")
        val userId = currentUser.uid

        val boardRef = firestore.collection("boards").document()
        val boardId = boardRef.id

        val newBoard = Board(
            docId = boardId,
            name = name,
            cover = cover,
            ownerId = userId,
            memberIds = listOf(userId),
            createdBy = userId,
            isLoading = null
        )

        _boards.update { it + newBoard.copy(isLoading = true) }

        try {
            boardRef.set(newBoard).await()

            _boards.update { list ->
                list.map {
                    if (it.docId == boardId) {
                        newBoard.copy(isLoading = false)
                    } else {
                        it
                    }
                }
            }
        } catch (e: Exception) {
            _boards.update { list -> list.filterNot { it.docId == boardId } }
            throw e
        }
    }

    override suspend fun deleteBoard(docId: String) {
        val currentUser = auth.currentUser ?: throw Exception("User not logged in")
        val boardRef = firestore.collection("boards").document(docId)

        val boardSnapshot = boardRef.get().await()
        val boardData = boardSnapshot.toObject(Board::class.java)
        if (boardData?.ownerId != currentUser.uid) {
            throw SecurityException("Only the board owner can delete the board.")
        }

        boardRef.delete().await()
        _boards.update { list -> list.filterNot { it.docId == docId } }
    }

    override suspend fun deleteAllBoards() {
        val currentUser = auth.currentUser ?: throw Exception("User not logged in")
        val userId = currentUser.uid

        val boardsToDeleteQuery = firestore.collection("boards")
            .whereEqualTo("ownerId", userId)

        val previousState = _boards.value
        try {
            _boards.update { list -> list.filterNot { it.ownerId == userId } }

            val snapshot = boardsToDeleteQuery.get().await()
            if (snapshot.isEmpty) return

            val batch = firestore.batch()
            snapshot.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()

        } catch (e: Exception) {
            _boards.value = previousState
            throw e
        }
    }


    override suspend fun updateBoardName(docId: String, name: String) {
        val currentUser = auth.currentUser ?: throw Exception("User not logged in")
        val boardRef = firestore.collection("boards").document(docId)

        val boardSnapshot = boardRef.get().await()
        val boardData = boardSnapshot.toObject(Board::class.java)
        if (boardData == null || (!boardData.memberIds.contains(currentUser.uid) && boardData.ownerId != currentUser.uid)) {
            throw SecurityException("User must be a member to update the board name.")
        }

        boardRef.update("name", name).await()
        _boards.update { list -> list.map { if (it.docId == docId) it.copy(name = name) else it } }
    }

    override suspend fun updateBoardCover(docId: String, color: Int?) {
        val currentUser = auth.currentUser ?: throw Exception("User not logged in")
        val boardRef = firestore.collection("boards").document(docId)

        val boardSnapshot = boardRef.get().await()
        val boardData = boardSnapshot.toObject(Board::class.java)
        if (boardData == null || (!boardData.memberIds.contains(currentUser.uid) && boardData.ownerId != currentUser.uid)) {
            throw SecurityException("User must be a member to update the board cover.")
        }

        boardRef.update("cover", color).await()
        _boards.update { list -> list.map { if (it.docId == docId) it.copy(cover = color) else it } }
    }

    override suspend fun removeNoteFromNotelist(notelistId: String, noteId: String) {
        val currentUser = auth.currentUser ?: throw Exception("User not logged in")
        val notelistRef = firestore.collection("notelists").document(notelistId)

        firestore.runTransaction { transaction ->
            val notelistSnapshot = transaction.get(notelistRef)
            val notelist = notelistSnapshot.toObject(Notelist::class.java)

            if (notelist == null) {
                throw Exception("Notelist not found.")
            }

            val boardId = notelist.boardId ?: throw Exception("Notelist has no parent board ID.")
            val boardRef = firestore.collection("boards").document(boardId)
            val boardSnapshot = transaction.get(boardRef)
            val board = boardSnapshot.toObject(Board::class.java)

            if (board == null || (!board.memberIds.contains(currentUser.uid) && board.ownerId != currentUser.uid)) {
                throw SecurityException("User must be a member or owner to modify this notelist.")
            }

            val updatedNotes = notelist.notes.filter { it.docId != noteId }

            transaction.update(notelistRef, "notes", updatedNotes)

        }.await()
        _notelists.update { currentNotelists ->
            currentNotelists.map { nl ->
                if (nl.docId == notelistId) {
                    val updatedNotes = nl.notes.filter { note -> note.docId != noteId }
                    nl.copy(notes = updatedNotes)
                } else {
                    nl
                }
            }
        }
    }

    override suspend fun addNotelist(boardId: String, notelist: Notelist) {
        val currentUser = auth.currentUser ?: throw Exception("User not logged in")
        val boardRef = firestore.collection("boards").document(boardId)

        val boardSnapshot = boardRef.get().await()
        val boardData = boardSnapshot.toObject(Board::class.java)
        if (boardData == null || (!boardData.memberIds.contains(currentUser.uid) && boardData.ownerId != currentUser.uid)) {
            throw SecurityException("User must be a member to add a notelist to this board.")
        }

        val notelistRef = firestore.collection("notelists").document()
        val newNotelistWithId = notelist.copy(
            docId = notelistRef.id,
            boardId = boardId,
            notes = emptyList()
        )

        notelistRef.set(newNotelistWithId).await()
    }


    override suspend fun addNoteToList(notelistId: String, note: Note) {
        val currentUser = auth.currentUser ?: throw Exception("User not logged in")
        val notelistRef = firestore.collection("notelists").document(notelistId)

        firestore.runTransaction { transaction ->
            val notelistSnapshot = transaction.get(notelistRef)
            val notelist = notelistSnapshot.toObject(Notelist::class.java)
            if (notelist == null) {
                throw Exception("Notelist not found.")
            }

            val boardId = notelist.boardId ?: throw Exception("Notelist has no parent board ID.")
            val boardRef = firestore.collection("boards").document(boardId)
            val boardSnapshot = transaction.get(boardRef)
            val board = boardSnapshot.toObject(Board::class.java)

            if (board == null || (!board.memberIds.contains(currentUser.uid) && board.ownerId != currentUser.uid)) {
                throw SecurityException("User must be a member or owner to add a note to this notelist.")
            }

            val newNoteWithId = note.copy(docId = firestore.collection("notes").document().id)
            val updatedNotes = notelist.notes + newNoteWithId

            transaction.update(notelistRef, "notes", updatedNotes)
        }.await()
    }

    override suspend fun removeNotelist(notelistId: String) {
        val currentUser = auth.currentUser ?: throw Exception("User not logged in")
        val notelistRef = firestore.collection("notelists").document(notelistId)

        val notelistSnapshot = notelistRef.get().await()
        val notelist = notelistSnapshot.toObject(Notelist::class.java) ?: throw Exception("Notelist not found.")

        val boardRef = firestore.collection("boards").document(notelist.boardId!!)
        val boardSnapshot = boardRef.get().await()
        val board = boardSnapshot.toObject(Board::class.java)

        if (board == null || (!board.memberIds.contains(currentUser.uid) && board.ownerId != currentUser.uid)) {
            throw SecurityException("User must be a member or owner to remove this notelist.")
        }

        notelistRef.delete().await()
    }


    override suspend fun addMemberToBoard(boardId: String, userIdToAdd: String): Boolean {
        val currentUser = auth.currentUser ?: throw Exception("User not logged in")
        val boardRef = firestore.collection("boards").document(boardId)

        val boardSnapshot = boardRef.get().await()
        val board = boardSnapshot.toObject(Board::class.java)
        if (board == null || board.ownerId != currentUser.uid) {
            Log.e("BoardRepository", "Only the owner can add members. Current user: ${currentUser.uid}, Owner: ${board?.ownerId}")
            throw SecurityException("Only the board owner can add members.")
        }

        if (board.memberIds.contains(userIdToAdd)) {
            Log.i("BoardRepository", "User $userIdToAdd is already a member of board $boardId.")
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
            Log.w("BoardRepository", "Owner cannot remove themselves from the members list directly. Consider transferring ownership.")
        }


        boardRef.update("memberIds", FieldValue.arrayRemove(userIdToRemove)).await()
        _boards.update { list ->
            list.map {
                if (it.docId == boardId) it.copy(memberIds = it.memberIds - userIdToRemove) else it
            }
        }
        return true
    }

    override suspend fun refreshNotelists(boardId: String) {
        val notelistsQuery = firestore.collection("notelists")
            .whereEqualTo("boardId", boardId)
            .get()
            .await()
        // This is a one-time fetch, useful for initial loading.
        val notelistList = notelistsQuery.documents.mapNotNull {
            it.toObject(Notelist::class.java)
        }
    }
    override fun getNotelistsForBoard(boardId: String?): Flow<List<Notelist>> {
        if (boardId == null) {
            return flowOf(emptyList())
        }

        return callbackFlow {
            val notelistsQuery = firestore.collection("notelists")
                .whereEqualTo("boardId", boardId)

            val listenerRegistration = notelistsQuery.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val notelistList = snapshot.documents.mapNotNull {
                        it.toObject(Notelist::class.java)
                    }
                    trySend(notelistList)
                }
            }
            awaitClose {
                listenerRegistration.remove()
            }
        }
    }

    override fun registerNotelistListener(boardId: String) {
        val currentUser = auth.currentUser ?: throw Exception("User not logged in")

        // First, remove any existing listener to prevent duplicates.
        removeNotelistListener()

        val notelistsRef = firestore.collection("notelists")
            .whereEqualTo("boardId", boardId)

        // Set up the real-time listener.
        notelistListener = notelistsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("BoardRepository", "Listen notelists snapshot failed", error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val notelistList = snapshot.documents.mapNotNull {
                    it.toObject(Notelist::class.java)
                }
                _notelists.value = notelistList
            }
        }
    }

    override fun removeNotelistListener() {
        notelistListener?.remove() // Disconnects the listener from the database.
        notelistListener = null
        _notelists.value = emptyList() // Clear the data in the StateFlow.
    }

    override fun clearCache() {
        removeListener()
        _boards.value = emptyList()
    }
}

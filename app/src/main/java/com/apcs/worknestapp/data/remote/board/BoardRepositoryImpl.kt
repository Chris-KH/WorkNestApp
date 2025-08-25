package com.apcs.worknestapp.data.remote.board

import android.util.Log
import com.apcs.worknestapp.data.remote.note.Note
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BoardRepositoryImpl @Inject constructor() : BoardRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _boards = MutableStateFlow<List<Board>>(emptyList())
    override val board: StateFlow<List<Board>> = _boards

    private var boardListener: ListenerRegistration? = null

    init {
        auth.addAuthStateListener {
            if (it.currentUser == null) removeListener()
        }
    }

    override fun removeListener() {
        boardListener?.remove()
        boardListener = null
    }

    override fun registerListener() {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val boardsRef = firestore
            .collection("users")
            .document(authUser.uid)
            .collection("boards")

        boardListener?.remove()
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
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val boardsRef = firestore
            .collection("users")
            .document(authUser.uid)
            .collection("boards")

        val snapshot = boardsRef.get().await()
        val boardList = snapshot.documents.mapNotNull {
            it.toObject(Board::class.java)
        }
        _boards.value = boardList
    }

    override suspend fun getBoard(docId: String): Board {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val boardDoc = firestore.collection("users")
            .document(authUser.uid)
            .collection("boards")
            .document(docId)
            .get()
            .await()

        return boardDoc.toObject(Board::class.java) ?: throw Exception("Invalid board format")
    }

    override suspend fun addBoard(board: Board) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val boardRef = firestore.collection("users")
            .document(authUser.uid)
            .collection("boards")
            .document()

        val boardId = boardRef.id
        val newBoard = board.copy(docId = boardId, isLoading = null)

        _boards.value = _boards.value + newBoard.copy(isLoading = true)

        boardRef.set(newBoard).await()
        val snapshot = boardRef.get().await()
        val savedBoard = snapshot.toObject(Board::class.java)

        savedBoard?.let {
            _boards.update { list -> list.filterNot { it.docId == boardId } }
            _boards.value = _boards.value + it
        }
    }


    override suspend fun deleteBoard(docId: String) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        try {
            firestore.collection("users")
                .document(authUser.uid)
                .collection("boards")
                .document(docId)
                .delete()
                .await()

            _boards.update { list -> list.filterNot { it.docId == docId } }
        } catch (_: Exception) {
            _boards.update { list -> list.filterNot { it.docId == docId } }
        }
    }

    override suspend fun deleteAllBoards() {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val boardRef = firestore.collection("users")
            .document(authUser.uid)
            .collection("boards")

        val previousState = _boards.value

        try {
            _boards.value = emptyList()

            val snapshot = boardRef.get().await()
            val batch = firestore.batch()
            snapshot.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()
        } catch (e: Exception) {
            _boards.value = previousState
            throw e
        }
    }

    override suspend fun updateBoardName(docId: String, name: String) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val boardRef = firestore.collection("users")
            .document(authUser.uid)
            .collection("boards")
            .document(docId)

        boardRef.update("name", name).await()
        _boards.update { list -> list.map { if (it.docId == docId) it.copy(name = name) else it } }
    }

    override suspend fun updateBoardCover(docId: String, color: Int?) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val boardRef = firestore.collection("users")
            .document(authUser.uid)
            .collection("boards")
            .document(docId)

        boardRef.update("cover", color).await()
        _boards.update { list -> list.map { if (it.docId == docId) it.copy(cover = color) else it } }
    }

    override suspend fun addNote(docId: String, note: Note) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val boardRef = firestore.collection("users")
            .document(authUser.uid)
            .collection("boards")
            .document(docId)

        val newNotelist = Notelist(
            docId = null,
            name = note.name,
            cover = note.cover,
            archived = false,
            createdAt = Timestamp.now(),
            notes = listOf(note)
        )

        boardRef.update("notelists", FieldValue.arrayUnion(newNotelist)).await()
    }

    override suspend fun removeNoteFromBoard(docId: String, noteId: String) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val boardRef = firestore.collection("users")
            .document(authUser.uid)
            .collection("boards")
            .document(docId)

        val snapshot = boardRef.get().await()
        val board = snapshot.toObject(Board::class.java) ?: return

        val updatedNotelists = board.notelists.map { notelist ->
            notelist.copy(notes = notelist.notes.filter { it.docId != noteId })
        }

        boardRef.update("notelists", updatedNotelists).await()
        _boards.update { list -> list.map { if (it.docId == docId) it.copy(notelists = updatedNotelists) else it } }
    }

    override suspend fun addMember() {
        //TODO: throw NotImplementedError("Depends on how user references are structured")
    }

    override fun clearCache() {
        removeListener()
        _boards.value = emptyList()
    }
}

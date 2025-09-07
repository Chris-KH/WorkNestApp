package com.apcs.worknestapp.data.remote.board

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.apcs.worknestapp.data.remote.note.Note
import com.apcs.worknestapp.data.remote.user.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.String

class BoardRepositoryImpl @Inject constructor() : BoardRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("BoardRepository", "Coroutine crashed", throwable)
    }
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + errorHandler)

    private var boardListener: ListenerRegistration? = null
    private var noteListListener: ListenerRegistration? = null
    private var notesListener = mutableMapOf<String, ListenerRegistration>()

    private val _boards = MutableStateFlow<List<Board>>(emptyList())
    override val boards: StateFlow<List<Board>> = _boards.asStateFlow()

    private val _currentBoard = MutableStateFlow<Board?>(null)
    override val currentBoard: StateFlow<Board?> = _currentBoard.asStateFlow()

    init {
        auth.addAuthStateListener {
            if (it.currentUser == null) {
                clearCache()
            } else {
                registerBoardListener()
            }
        }
    }

    private fun boardNotFound(boardId: String) {
        if (_currentBoard.value?.docId == boardId) _currentBoard.value = null
        _boards.update { list -> list.filterNot { it.docId == boardId } }
    }

    private fun noteListNotFound(boardId: String, noteListId: String) {
        if (_currentBoard.value?.docId == boardId) {
            _currentBoard.update { board ->
                board?.copy(noteLists = board.noteLists.filterNot { it.docId == noteListId })
            }
        }
    }

    // *OK
    override fun registerBoardListener() {
        val authUser = auth.currentUser
        if (authUser == null) {
            registerBoardListener()
            throw Exception("User not logged in")
        }
        if (boardListener != null) return

        val boardsRef = firestore.collection("boards")
            .where(
                Filter.or(
                    Filter.equalTo("ownerId", authUser.uid),
                    Filter.arrayContains("memberIds", authUser.uid)
                )
            )

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
                val allBoards = (remoteBoards + pendingBoards).sortedByDescending { it.createdAt }
                if (_currentBoard.value != null) {
                    val currentBoardId = _currentBoard.value!!.docId
                    val board = allBoards.find { it.docId == currentBoardId }
                    board?.let { new ->
                        _currentBoard.update { current ->
                            current?.copy(
                                name = new.name,
                                cover = new.cover,
                                description = new.description,
                                showNoteCover = new.showNoteCover,
                                showCompletedStatus = new.showCompletedStatus,
                                ownerId = new.ownerId,
                                memberIds = new.memberIds,
                                members = current.members.filterNot { member ->
                                    new.memberIds.none { it == member.docId }
                                }
                            )
                        }
                    }
                }
                _boards.value = allBoards
            }
        }
    }

    // *OK
    override fun removeBoardListener() {
        boardListener?.remove()
        boardListener = null
    }

    // *OK
    override fun registerNoteListListener(boardId: String) {
        val authUser = auth.currentUser
        if (authUser == null) {
            removeNoteListListener()
            throw Exception("User not logged in")
        }
        if (noteListListener != null) return

        val noteListsRef = firestore.collection("boards").document(boardId).collection("notelists")
        noteListListener = noteListsRef.addSnapshotListener { snapshot, error ->
            val currentBoardId = _currentBoard.value?.docId
            if (currentBoardId == null || currentBoardId != boardId) return@addSnapshotListener

            if (error != null) {
                if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    boardNotFound(boardId)
                }
                Log.e("BoardRepository", "Listen notelists snapshot failed", error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                _currentBoard.update { board ->
                    val currentLists = board?.noteLists?.toMutableList() ?: mutableListOf()

                    for(change in snapshot.documentChanges) {
                        val noteList = change.document.toObject(NoteList::class.java)
                        val noteListId = change.document.id

                        when(change.type) {
                            DocumentChange.Type.ADDED -> {
                                if (currentLists.none { it.docId == noteList.docId }) {
                                    currentLists.add(noteList)
                                }
                                registerNoteListener(boardId, noteListId)
                            }

                            DocumentChange.Type.MODIFIED -> {
                                val index = currentLists.indexOfFirst { it.docId == noteList.docId }
                                if (index != -1) {
                                    val tempList = currentLists[index]
                                    currentLists[index] = tempList.copy(
                                        name = noteList.name,
                                        archived = noteList.archived,
                                    )
                                }
                            }

                            DocumentChange.Type.REMOVED -> {
                                currentLists.removeAll { it.docId == noteList.docId }
                                notesListener[noteListId]?.remove()
                                notesListener.remove(noteListId)
                            }
                        }
                    }

                    board?.copy(noteLists = currentLists)
                }
            }
        }
    }

    // *OK
    override fun removeNoteListListener() {
        noteListListener?.remove()
        noteListListener = null
    }

    // *OK
    override fun registerNoteListener(boardId: String, noteListId: String) {
        val authUser = auth.currentUser
        if (authUser == null) {
            removeNoteListListener()
            throw Exception("User not logged in")
        }
        if (notesListener[noteListId] != null) return

        val notesRef = firestore
            .collection("boards")
            .document(boardId)
            .collection("notelists")
            .document(noteListId)
            .collection("notes")

        notesListener[boardId] = notesRef.addSnapshotListener { snapshot, error ->
            val currentBoardId = _currentBoard.value?.docId
            if (currentBoardId == null || currentBoardId != boardId) return@addSnapshotListener

            if (error != null) {
                if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    boardNotFound(boardId)
                }
                return@addSnapshotListener
            }

            if (snapshot != null) {
                _currentBoard.update { board ->
                    val currentLists = board?.noteLists?.toMutableList() ?: mutableListOf()
                    val listIndex = currentLists.indexOfFirst { it.docId == noteListId }
                    if (listIndex == -1) return@update board

                    val targetList = currentLists[listIndex]
                    val notes = targetList.notes.toMutableList()

                    for(change in snapshot.documentChanges) {
                        val note = change.document.toObject(Note::class.java)

                        when(change.type) {
                            DocumentChange.Type.ADDED -> {
                                if (notes.none { it.docId == note.docId }) {
                                    notes.add(note)
                                }
                            }

                            DocumentChange.Type.MODIFIED -> {
                                val idx = notes.indexOfFirst { it.docId == note.docId }
                                if (idx != -1) {
                                    notes[idx] = note
                                }
                            }

                            DocumentChange.Type.REMOVED -> {
                                notes.removeAll { it.docId == note.docId }
                            }
                        }
                    }

                    currentLists[listIndex] = targetList.copy(notes = notes)
                    board?.copy(noteLists = currentLists)
                }
            }
        }
    }

    // *OK
    override fun removeNoteListener() {
        notesListener.forEach { it.value.remove() }
        notesListener.clear()
    }

    // *OK
    override fun addBoard(board: Board) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        repoScope.launch {
            val boardRef = firestore.collection("boards").document()
            val boardId = boardRef.id

            val tempBoard = board.copy(
                docId = boardId,
                ownerId = authUser.uid,
                memberIds = listOf(authUser.uid),
                showNoteCover = true,
                showCompletedStatus = true,
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

    // *OK
    override fun deleteBoard(docId: String) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        if (_currentBoard.value?.docId == docId) {
            if (authUser.uid != _currentBoard.value!!.ownerId
                && _currentBoard.value!!.adminIds.none { it == authUser.uid }
            )
                throw Exception("Missing permission to delete this board")
        }

        repoScope.launch {
            try {
                firestore.collection("boards").document(docId).delete().await()

                withContext(Dispatchers.Main) { boardNotFound(docId) }
            } catch(e: FirebaseFirestoreException) {
                if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                    withContext(Dispatchers.Main) { boardNotFound(docId) }
                }
            }
        }
    }

    // *OK - NOT USE
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

    // *OK
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

    // *OK
    override suspend fun getBoard(docId: String): Board {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val boardRef = firestore.collection("boards").document(docId)
        val boardDoc = try {
            boardRef.get().await()
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) boardNotFound(docId)
            throw Exception("Don't have permission")
        } catch(e: Exception) {
            throw e
        }

        if (!boardDoc.exists()) throw Exception("Board not found")
        val board = boardDoc.toObject(Board::class.java) ?: throw Exception("Invalid board format")
        if (board.ownerId != authUser.uid && !board.memberIds.contains(authUser.uid)) {
            throw SecurityException("User does not have access to this board.")
        }

        val noteListsSnapshot = boardRef.collection("notelists").get().await()
        val noteLists = coroutineScope {
            noteListsSnapshot.documents.map { noteListDoc ->
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
        val members = coroutineScope {
            board.memberIds.map { memberId ->
                async {
                    try {
                        val memberSnapshot = firestore.collection("users").document(memberId)
                            .get().await()
                        memberSnapshot.toObject(User::class.java)
                    } catch(_: Exception) {
                        null
                    }
                }
            }.awaitAll().filterNotNull()
        }

        val result = board.copy(noteLists = noteLists, members = members)
        _currentBoard.value = result
        return result
    }

    // *OK
    override suspend fun updateBoardName(docId: String, name: String) {
        auth.currentUser ?: throw IllegalStateException("User not logged in")
        val boardRef = firestore.collection("boards").document(docId)

        try {
            boardRef.update("name", name).await()
            if (_currentBoard.value?.docId == docId) {
                _currentBoard.update { it?.copy(name = name) }
            }
            _boards.update { list ->
                list.map { if (it.docId == docId) it.copy(name = name) else it }
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                boardNotFound(docId)
                throw Exception("Board not found")
            } else if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                boardNotFound(docId)
                throw Exception("Missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    // * OK
    override suspend fun updateBoardDescription(docId: String, description: String) {
        auth.currentUser ?: throw IllegalStateException("User not logged in")
        val boardRef = firestore.collection("boards").document(docId)

        try {
            boardRef.update("description", description).await()
            if (_currentBoard.value?.docId == docId) {
                _currentBoard.update { it?.copy(description = description) }
            }
            _boards.update { list ->
                list.map { if (it.docId == docId) it.copy(description = description) else it }
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                boardNotFound(docId)
                throw Exception("Board not found")
            } else if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                boardNotFound(docId)
                throw Exception("Missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    // *OK
    override suspend fun updateBoardShowNoteCover(docId: String, showNoteCover: Boolean) {
        auth.currentUser ?: throw IllegalStateException("User not logged in")
        val boardRef = firestore.collection("boards").document(docId)

        try {
            boardRef.update("showNoteCover", showNoteCover).await()
            if (_currentBoard.value?.docId == docId) {
                _currentBoard.update { it?.copy(showNoteCover = showNoteCover) }
            }
            _boards.update { list ->
                list.map { if (it.docId == docId) it.copy(showNoteCover = showNoteCover) else it }
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                boardNotFound(docId)
                throw Exception("Board not found")
            } else if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                boardNotFound(docId)
                throw Exception("Missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    // *OK
    override suspend fun updateBoardShowCompletedStatus(
        docId: String,
        showCompletedStatus: Boolean,
    ) {
        auth.currentUser ?: throw IllegalStateException("User not logged in")
        val boardRef = firestore.collection("boards").document(docId)

        try {
            boardRef.update("showCompletedStatus", showCompletedStatus).await()
            if (_currentBoard.value?.docId == docId) {
                _currentBoard.update { it?.copy(showCompletedStatus = showCompletedStatus) }
            }
            _boards.update { list ->
                list.map { if (it.docId == docId) it.copy(showCompletedStatus = showCompletedStatus) else it }
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                boardNotFound(docId)
                throw Exception("Board not found")
            } else if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                boardNotFound(docId)
                throw Exception("Missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    // *OK
    override suspend fun updateBoardCover(docId: String, color: Int?) {
        auth.currentUser ?: throw Exception("User not logged in")
        val boardRef = firestore.collection("boards").document(docId)

        try {
            boardRef.update("cover", color).await()
            if (_currentBoard.value?.docId == docId) {
                _currentBoard.update { it?.copy(cover = color) }
            }
            _boards.update { list ->
                list.map {
                    if (it.docId == docId) it.copy(cover = color) else it
                }
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                boardNotFound(docId)
                throw Exception("Board not found")
            } else if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                boardNotFound(docId)
                throw Exception("Missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    // *OK
    override suspend fun addMemberToBoard(boardId: String, user: User) {
        auth.currentUser ?: throw Exception("User not logged in")
        val boardRef = firestore.collection("boards").document(boardId)
        val userIdToAdd = user.docId ?: throw Exception("User not founded")

        try {
            boardRef.update("memberIds", FieldValue.arrayUnion(userIdToAdd)).await()
            if (_currentBoard.value?.docId == boardId) {
                _currentBoard.update {
                    it?.copy(
                        memberIds = (it.memberIds + userIdToAdd).distinct(),
                        members = if (it.members.none { member -> member.docId == userIdToAdd }) it.members + user
                        else it.members
                    )
                }
            }
            _boards.update { list ->
                list.map {
                    if (it.docId == boardId) it.copy(memberIds = (it.memberIds + userIdToAdd).distinct())
                    else it
                }
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                boardNotFound(boardId)
                throw Exception("Board not found")
            } else if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                boardNotFound(boardId)
                throw Exception("Missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    // *OK
    override suspend fun removeMemberFromBoard(boardId: String, user: User) {
        auth.currentUser ?: throw Exception("User not logged in")
        val boardRef = firestore.collection("boards").document(boardId)
        val userIdToRemove = user.docId ?: throw Exception("User not founded")

        try {
            boardRef.update("memberIds", FieldValue.arrayRemove(userIdToRemove)).await()
            if (_currentBoard.value?.docId == boardId) {
                _currentBoard.update {
                    it?.copy(
                        memberIds = (it.memberIds - userIdToRemove).distinct(),
                        members = it.members.filterNot { member -> member.docId == userIdToRemove }
                    )
                }
            }
            _boards.update { list ->
                list.map {
                    if (it.docId == boardId) it.copy(memberIds = it.memberIds - userIdToRemove) else it
                }
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                boardNotFound(boardId)
                throw Exception("Board not found")
            } else if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                boardNotFound(boardId)
                throw Exception("Missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    // *OK
    override suspend fun addNoteList(boardId: String, noteList: NoteList) {
        auth.currentUser ?: throw Exception("User not logged in")
        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document()

        try {
            firestore.runTransaction { transaction ->
                val boardSnapshot = transaction.get(boardRef)
                if (!boardSnapshot.exists()) {
                    boardNotFound(boardId)
                    throw Exception("Board not found")
                }

                val noteListId = noteListRef.id
                val newNoteList = noteList.copy(
                    docId = noteListId,
                    createdAt = Timestamp.now()
                )
                transaction.set(noteListRef, newNoteList)
                if (_currentBoard.value?.docId == boardId) {
                    _currentBoard.update { board ->
                        if (board?.docId == boardId) board.copy(
                            noteLists = board.noteLists + newNoteList
                        )
                        else board
                    }
                    registerNoteListener(boardId, noteListId)
                }
                _boards.update { list ->
                    list.map { board ->
                        if (board.docId == boardId) board.copy(
                            noteLists = board.noteLists + newNoteList
                        )
                        else board
                    }
                }
            }.await()
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                boardNotFound(boardId)
                throw Exception("Board not found")
            } else if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                boardNotFound(boardId)
                throw Exception("Missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    // *OK
    override suspend fun removeNoteList(boardId: String, noteListId: String) {
        auth.currentUser ?: throw Exception("User not logged in")
        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)

        try {
            firestore.runTransaction { transaction ->
                val boardSnapshot = transaction.get(boardRef)
                if (!boardSnapshot.exists()) {
                    boardNotFound(boardId)
                    throw Exception("Board not found")
                }

                transaction.delete(noteListRef)
                notesListener[noteListId]?.remove()
                notesListener.remove(noteListId)
                if (_currentBoard.value?.docId == boardId) {
                    _currentBoard.update { board ->
                        if (board?.docId == boardId) board.copy(
                            noteLists = board.noteLists.filterNot { it.docId == noteListId }
                        )
                        else board
                    }
                }
                _boards.update { list ->
                    list.map { board ->
                        if (board.docId == boardId) board.copy(
                            noteLists = board.noteLists.filterNot { it.docId == noteListId }
                        )
                        else board
                    }
                }
            }.await()
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                boardNotFound(boardId)
                notesListener[noteListId]?.remove()
                notesListener.remove(noteListId)
                throw Exception("Board not found")
            } else if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                boardNotFound(boardId)
                notesListener[noteListId]?.remove()
                notesListener.remove(noteListId)
                throw Exception("Missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    // *OK
    override suspend fun updateNoteListName(boardId: String, noteListId: String, name: String) {
        auth.currentUser ?: throw Exception("User not logged in")
        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)

        try {
            firestore.runTransaction { transaction ->
                val boardSnapshot = transaction.get(boardRef)
                if (!boardSnapshot.exists()) {
                    boardNotFound(boardId)
                    throw Exception("Board not found")
                }

                transaction.update(noteListRef, "name", name)
                if (_currentBoard.value?.docId == boardId) {
                    _currentBoard.update { board ->
                        if (board?.docId == boardId) board.copy(
                            noteLists = board.noteLists.map { noteList ->
                                if (noteList.docId == noteListId) noteList.copy(name = name)
                                else noteList
                            }
                        )
                        else board
                    }
                }
                _boards.update { list ->
                    list.map { board ->
                        if (board.docId == boardId) board.copy(
                            noteLists = board.noteLists.map { noteList ->
                                if (noteList.docId == noteListId) noteList.copy(name = name)
                                else noteList
                            }
                        )
                        else board
                    }
                }
            }.await()
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                boardNotFound(boardId)
                notesListener[noteListId]?.remove()
                notesListener.remove(noteListId)
                throw Exception("Board not found")
            } else if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                boardNotFound(boardId)
                notesListener[noteListId]?.remove()
                notesListener.remove(noteListId)
                throw Exception("Missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    override suspend fun addNoteToList(boardId: String, noteListId: String, note: Note) {
        auth.currentUser ?: throw Exception("User not logged in")
        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)
        val noteRef = noteListRef.collection("notes").document()
        val noteId = noteRef.id

        try {
            firestore.runTransaction { transaction ->
                val boardSnapshot = transaction.get(boardRef)
                if (!boardSnapshot.exists()) {
                    boardNotFound(boardId)
                    throw Exception("Board not found")
                }
                val noteListSnapshot = transaction.get(noteListRef)
                if (!noteListSnapshot.exists()) {
                    noteListNotFound(boardId, noteListId)
                    throw Exception("Note list not found")
                }

                val newNote = note.copy(
                    docId = noteId,
                    createdAt = Timestamp.now(),
                )
                transaction.set(noteRef, newNote)
                if (_currentBoard.value?.docId == boardId) {
                    _currentBoard.update { board ->
                        if (board?.docId == boardId) board.copy(
                            noteLists = board.noteLists.map { noteList ->
                                if (noteList.docId == noteListId)
                                    noteList.copy(notes = noteList.notes + newNote)
                                else noteList
                            }
                        )
                        else board
                    }
                }
            }.await()
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                boardNotFound(boardId)
                notesListener[noteListId]?.remove()
                notesListener.remove(noteListId)
                throw Exception("Board not found")
            } else if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                boardNotFound(boardId)
                notesListener[noteListId]?.remove()
                notesListener.remove(noteListId)
                throw Exception("Missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    override suspend fun removeNoteFromList(boardId: String, noteListId: String, noteId: String) {
        val currentUser = auth.currentUser ?: throw Exception("User not logged in")
        val noteRef = firestore.collection("boards").document(boardId)
            .collection("notelists").document(noteListId)
            .collection("notes").document(noteId)

        val boardDoc = firestore.collection("boards").document(boardId).get().await()
        val boardData = boardDoc.toObject(Board::class.java)
        if (boardData == null || (!boardData.memberIds.contains(currentUser.uid) && boardData.ownerId != currentUser.uid)) {
            throw SecurityException("User must be a member/owner to remove this note.")
        }

        noteRef.delete().await()
    }


    override suspend fun updateNoteCheckedStatus(
        boardId: String,
        noteListId: String,
        noteId: String,
        isChecked: Boolean,
    ) {
        val authUser = auth.currentUser

        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)
    }

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

    override fun clearCache() {
        repoScope.coroutineContext.cancelChildren()
        removeBoardListener()
        removeNoteListListener()
        removeNoteListener()
        _currentBoard.value = null
        _boards.value = emptyList()
    }
}

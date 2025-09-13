package com.apcs.worknestapp.data.remote.board

import android.util.Log
import com.apcs.worknestapp.data.remote.note.Checklist
import com.apcs.worknestapp.data.remote.note.Comment
import com.apcs.worknestapp.data.remote.note.Note
import com.apcs.worknestapp.data.remote.note.Task
import com.apcs.worknestapp.data.remote.user.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
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

class BoardRepositoryImpl @Inject constructor() : BoardRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("BoardRepository", "Coroutine crashed", throwable)
    }
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + errorHandler)

    private var boardsListener: ListenerRegistration? = null

    private var currentBoardListener: ListenerRegistration? = null
    private var noteListsListener: ListenerRegistration? = null
    private var notesListener = mutableMapOf<String, ListenerRegistration>()

    //Note detail
    private var currentNoteListener: ListenerRegistration? = null
    private var checklistsListener: ListenerRegistration? = null
    private var commentsListener: ListenerRegistration? = null
    private var tasksListener = mutableMapOf<String, ListenerRegistration>()

    private val _boards = MutableStateFlow<List<Board>>(emptyList())
    override val boards: StateFlow<List<Board>> = _boards.asStateFlow()

    private val _currentBoard = MutableStateFlow<Board?>(null)
    override val currentBoard: StateFlow<Board?> = _currentBoard.asStateFlow()

    private val _currentNote = MutableStateFlow<Note?>(null)
    override val currentNote: StateFlow<Note?> = _currentNote.asStateFlow()

    init {
        auth.addAuthStateListener {
            if (it.currentUser == null) {
                clearCache()
            } else {
                registerBoardsListener()
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
            notesListener[noteListId]?.remove()
            notesListener.remove(noteListId)
        }
    }

    private fun noteNotFound(boardId: String, noteListId: String, noteId: String) {
        if (_currentNote.value?.docId == noteId) _currentNote.value = null
    }

    private fun checklistNotFound(
        boardId: String,
        noteListId: String,
        noteId: String,
        checklistId: String,
    ) {
        if (_currentNote.value?.docId == noteId) {
            _currentNote.update { note ->
                note?.copy(checklists = note.checklists.filterNot { it.docId == checklistId })
            }
        }
    }

    private fun taskNotFound(
        boardId: String,
        noteListId: String,
        noteId: String,
        checklistId: String,
        taskId: String,
    ) {
        if (_currentNote.value?.docId == noteId) {
            _currentNote.update { note ->
                note?.copy(checklists = note.checklists.map { checklist ->
                    if (checklist.docId == checklistId) {
                        checklist.copy(
                            tasks = checklist.tasks.filterNot { it.docId == taskId }
                        )
                    } else checklist
                })
            }
            tasksListener[taskId]?.remove()
            tasksListener.remove(taskId)
        }
    }

    private suspend fun getNoteLists(boardRef: DocumentReference): List<NoteList> = coroutineScope {
        val noteListsSnapshot = boardRef.collection("notelists").get().await()
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

    private suspend fun getUsers(ids: List<String>): List<User> = coroutineScope {
        ids.chunked(10).map { chunk ->
            async {
                val usersSnapshot =
                    firestore.collection("users").whereIn(FieldPath.documentId(), chunk).get()
                        .await()
                val users = usersSnapshot.documents.mapNotNull { it.toObject(User::class.java) }
                users
            }
        }.awaitAll().flatten()
    }

    // *OK
    override fun registerBoardsListener() {
        val authUser = auth.currentUser
        if (authUser == null) {
            registerBoardsListener()
            return
        }
        if (boardsListener != null) return

        val boardsRef = firestore.collection("boards").where(
            Filter.or(
                Filter.equalTo("ownerId", authUser.uid),
                Filter.arrayContains("memberIds", authUser.uid),
                Filter.arrayContains("adminIds", authUser.uid),
            )
        )

        boardsListener = boardsRef.addSnapshotListener { snapshot, error ->
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
                _boards.value = allBoards
            }
        }
    }

    // *OK
    override fun removeBoardsListener() {
        boardsListener?.remove()
        boardsListener = null
    }

    // *OK
    override fun registerCurrentBoardListener(boardId: String) {
        val authUser = auth.currentUser
        if (authUser == null) {
            removeCurrentBoardListener()
            return
        }

        val boardRef = firestore.collection("boards").document(boardId)
        val noteListsRef = boardRef.collection("notelists")
        fun registerNotesListener(noteListId: String) {
            val notesRef = noteListsRef.document(noteListId).collection("notes")

            notesListener[noteListId]?.remove()
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

        currentBoardListener = boardRef.addSnapshotListener { snapshot, error ->
            val currentBoardId = _currentBoard.value?.docId
            if (currentBoardId == null || currentBoardId != boardId) return@addSnapshotListener

            if (error != null) {
                if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    boardNotFound(boardId)
                }
                Log.e("BoardRepository", "Listen current board failed", error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val board = snapshot.toObject(Board::class.java)
                board?.let { new ->
                    val isSameMembers = _currentBoard.value!!.memberIds.groupingBy { it }
                        .eachCount() == new.memberIds.groupingBy { it }.eachCount()

                    _currentBoard.update { current ->
                        current?.copy(
                            name = new.name,
                            cover = new.cover,
                            description = new.description,
                            showNoteCover = new.showNoteCover,
                            showCompletedStatus = new.showCompletedStatus,
                            ownerId = new.ownerId,
                            memberIds = new.memberIds,
                        )
                    }

                    if (!isSameMembers) {
                        repoScope.launch {
                            val members = getUsers(new.memberIds)
                            withContext(Dispatchers.Main) {
                                _currentBoard.update { it?.copy(members = members) }
                            }
                        }
                    }
                }
            }
        }

        noteListsListener = noteListsRef.addSnapshotListener { snapshot, error ->
            val currentBoardId = _currentBoard.value?.docId
            if (currentBoardId == null || currentBoardId != boardId) return@addSnapshotListener

            if (error != null) {
                if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    boardNotFound(boardId)
                }
                Log.e("BoardRepository", "Listen notelists failed", error)
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
                                if (currentLists.none { it.docId == noteListId }) {
                                    currentLists.add(noteList)
                                }
                                registerNotesListener(noteListId)
                            }

                            DocumentChange.Type.MODIFIED -> {
                                val index = currentLists.indexOfFirst { it.docId == noteListId }
                                if (index != -1) {
                                    val tempList = currentLists[index]
                                    currentLists[index] = tempList.copy(
                                        name = noteList.name,
                                        archived = noteList.archived,
                                    )
                                }
                            }

                            DocumentChange.Type.REMOVED -> {
                                currentLists.removeAll { it.docId == noteListId }
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
    override fun removeCurrentBoardListener() {
        notesListener.forEach { it.value.remove() }
        notesListener.clear()
        noteListsListener?.remove()
        noteListsListener = null
        commentsListener?.remove()
        commentsListener = null
        currentBoardListener?.remove()
        currentBoardListener = null
    }

    override fun registerCurrentNoteListener(boardId: String, noteListId: String, noteId: String) {
        val authUser = auth.currentUser
        if (authUser == null) {
            removeCurrentNoteListener()
            return
        }
        removeCurrentNoteListener()

        val notesRef = firestore.collection("boards").document(boardId)
            .collection("notelists").document(noteListId).collection("notes").document(noteId)
        val checklistRef = notesRef.collection("checklists")
        val commentRef =
            notesRef.collection("comments").orderBy("createdAt", Query.Direction.DESCENDING)

        fun registerTaskListener(checklistId: String) {
            val tasksRef = checklistRef.document(checklistId).collection("tasks")
            tasksListener[checklistId]?.remove()
            tasksListener[checklistId] = tasksRef.addSnapshotListener { snapshot, error ->
                val currentNoteId = _currentNote.value?.docId
                if (currentNoteId == null || currentNoteId != noteId) return@addSnapshotListener

                if (error != null) {
                    Log.e("NoteRepository", "Listen note snapshot failed", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    _currentNote.update { note ->
                        val currentChecklists = note?.checklists?.toMutableList() ?: mutableListOf()
                        val listIndex = currentChecklists.indexOfFirst { it.docId == checklistId }
                        if (listIndex == -1) return@update note

                        val targetList = currentChecklists[listIndex]
                        val tasks = targetList.tasks.toMutableList()

                        for(change in snapshot.documentChanges) {
                            val task = change.document.toObject(Task::class.java)
                            val taskId = change.document.id

                            when(change.type) {
                                DocumentChange.Type.ADDED -> {
                                    if (tasks.none { it.docId == taskId }) {
                                        tasks.add(task)
                                    }
                                }

                                DocumentChange.Type.MODIFIED -> {
                                    val idx = tasks.indexOfFirst { it.docId == taskId }
                                    if (idx != -1) tasks[idx] = task
                                }

                                DocumentChange.Type.REMOVED -> {
                                    tasks.removeAll { it.docId == taskId }
                                }
                            }
                        }

                        currentChecklists[listIndex] = targetList.copy(tasks = tasks)
                        note?.copy(checklists = currentChecklists)
                    }
                }
            }
        }

        currentNoteListener = notesRef.addSnapshotListener { snapshot, error ->
            val currentNoteId = _currentNote.value?.docId
            if (currentNoteId == null || currentNoteId != noteId) return@addSnapshotListener

            if (error != null) {
                Log.e("NoteRepository", "Listen note snapshot failed", error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val note = snapshot.toObject(Note::class.java)
                note?.let {
                    _currentNote.update {
                        it?.copy(
                            name = note.name,
                            cover = note.cover,
                            description = note.description,
                            startDate = note.startDate,
                            endDate = note.endDate,
                            completed = note.completed,
                            archived = note.archived,
                        )
                    }
                }
            }
        }

        checklistsListener = checklistRef.addSnapshotListener { snapshot, error ->
            val currentNoteId = _currentNote.value?.docId
            if (currentNoteId == null || currentNoteId != noteId) return@addSnapshotListener

            if (error != null) {
                Log.e("NoteRepository", "Listen note snapshot failed", error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                _currentNote.update { note ->
                    val currentChecklists = note?.checklists?.toMutableList() ?: mutableListOf()

                    for(change in snapshot.documentChanges) {
                        val checklist = change.document.toObject(Checklist::class.java)
                        val checklistId = change.document.id

                        when(change.type) {
                            DocumentChange.Type.ADDED -> {
                                if (currentChecklists.none { it.docId == checklistId }) {
                                    currentChecklists.add(checklist)
                                }
                                registerTaskListener(checklistId)
                            }

                            DocumentChange.Type.MODIFIED -> {
                                val index =
                                    currentChecklists.indexOfFirst { it.docId == checklistId }
                                if (index != -1) {
                                    val tempList = currentChecklists[index]
                                    currentChecklists[index] = tempList.copy(
                                        name = checklist.name
                                    )
                                }
                            }

                            DocumentChange.Type.REMOVED -> {
                                currentChecklists.removeAll { it.docId == checklistId }
                                tasksListener[checklistId]?.remove()
                                tasksListener.remove(checklistId)
                            }
                        }
                    }

                    note?.copy(checklists = currentChecklists)
                }
            }
        }

        commentsListener = commentRef.addSnapshotListener { snapshot, error ->
            val currentNoteId = _currentNote.value?.docId
            if (currentNoteId == null || currentNoteId != noteId) return@addSnapshotListener

            if (error != null) {
                Log.e("NoteRepository", "Listen note comment snapshot failed", error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                _currentNote.update { note ->
                    val currentComments = note?.comments?.toMutableList() ?: mutableListOf()

                    for(change in snapshot.documentChanges) {
                        val comment = change.document.toObject(Comment::class.java)
                        val commentId = change.document.id

                        when(change.type) {
                            DocumentChange.Type.ADDED -> {
                                if (currentComments.none { it.docId == commentId }) {
                                    currentComments.add(comment)
                                }
                            }

                            DocumentChange.Type.MODIFIED -> {
                                val index =
                                    currentComments.indexOfFirst { it.docId == commentId }
                                if (index != -1) {
                                    val tempList = currentComments[index]
                                    currentComments[index] = tempList.copy(
                                        content = comment.content
                                    )
                                }
                            }

                            DocumentChange.Type.REMOVED -> {
                                currentComments.removeAll { it.docId == commentId }
                            }
                        }
                    }

                    note?.copy(comments = currentComments)
                }
            }
        }
    }

    override fun removeCurrentNoteListener() {
        tasksListener.forEach { it.value.remove() }
        tasksListener.clear()
        checklistsListener?.remove()
        checklistsListener = null
        currentNoteListener?.remove()
        currentNoteListener = null
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
                        (list.filterNot { it.docId == boardId } + tempBoard.copy(isLoading = false)).sortedByDescending { it.createdAt }
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
            if (authUser.uid != _currentBoard.value!!.ownerId && _currentBoard.value!!.adminIds.none { it == authUser.uid }) throw Exception(
                "Missing permission to delete this board"
            )
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

    // *OK
    override suspend fun refreshBoard() {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val boardsSnapshot = firestore.collection("boards").where(
            Filter.or(
                Filter.equalTo("ownerId", authUser.uid),
                Filter.arrayContains("memberIds", authUser.uid)
            )
        ).get().await()

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

        val (noteLists, members) = coroutineScope {
            val noteListsDeferred = async { getNoteLists(boardRef) }
            val membersDeferred = async { getUsers(board.memberIds) }
            Pair(noteListsDeferred.await(), membersDeferred.await())
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
                        members = it.members.filterNot { member -> member.docId == userIdToRemove })
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
                throw Exception("Missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    override suspend fun leaveBoard(boardId: String) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val boardRef = firestore.collection("boards").document(boardId)
        val userIdToRemove = authUser.uid

        try {
            boardRef.update("memberIds", FieldValue.arrayRemove(userIdToRemove)).await()
            if (_currentBoard.value?.docId == boardId) {
                _currentBoard.value = null
            }
            _boards.update { list ->
                list.filterNot { it.docId == boardId }
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                boardNotFound(boardId)
                throw Exception("Board not found")
            } else if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
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
            val noteListId = noteListRef.id
            val newNoteList = noteList.copy(
                docId = noteListId, createdAt = Timestamp.now()
            )
            noteListRef.set(newNoteList)
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                boardNotFound(boardId)
                throw Exception("Board not found")
            } else if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                boardNotFound(boardId)
                throw Exception("Board not found or missing permission to perform this action")
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
                            noteLists = board.noteLists.filterNot { it.docId == noteListId })
                        else board
                    }
                }
            }.await()
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                noteListNotFound(boardId, noteListId)
                throw Exception("Note list not found")
            } else if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                boardNotFound(boardId)
                throw Exception("Board not found or missing permission to perform this action")
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
                transaction.update(noteListRef, "name", name)
                if (_currentBoard.value?.docId == boardId) {
                    _currentBoard.update { board ->
                        if (board?.docId == boardId) board.copy(
                            noteLists = board.noteLists.map { noteList ->
                                if (noteList.docId == noteListId) noteList.copy(name = name)
                                else noteList
                            })
                        else board
                    }
                }
            }.await()
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                noteListNotFound(boardId, noteListId)
                throw Exception("Note list not found")
            } else if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                boardNotFound(boardId)
                throw Exception("Board not found or missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    override suspend fun updateNoteListArchive(
        boardId: String,
        noteListId: String,
        newState: Boolean,
    ) {
        auth.currentUser ?: throw Exception("User not logged in")
        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)

        try {
            val notesSnapshot = noteListRef.collection("notes").get().await()
            firestore.runTransaction { transaction ->
                transaction.update(noteListRef, "archived", newState)

                for(doc in notesSnapshot.documents) {
                    transaction.update(doc.reference, "archivedByList", newState)
                }
            }.await()

            if (_currentBoard.value?.docId == boardId) {
                _currentBoard.update { board ->
                    board?.copy(
                        noteLists = board.noteLists.map { noteList ->
                            if (noteList.docId == noteListId) noteList.copy(
                                archived = newState,
                                notes = noteList.notes.map { it.copy(archivedByList = newState) }
                            )
                            else noteList
                        }
                    )
                }
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                noteListNotFound(boardId, noteListId)
                throw Exception("Note list not found")
            } else if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                boardNotFound(boardId)
                throw Exception("Board not found or missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw Exception("${if (newState) "Archive" else "Unarchived"} note list failed")
        }
    }

    // *OK
    override suspend fun addNoteToNoteList(boardId: String, noteListId: String, note: Note) {
        auth.currentUser ?: throw Exception("User not logged in")
        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)
        val noteRef = noteListRef.collection("notes").document()
        val noteId = noteRef.id

        try {
            firestore.runTransaction { transaction ->
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
            }.await()
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) { //Only occur when you is not a member
                boardNotFound(boardId)
                throw Exception("Board not found or missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    // *OK
    override suspend fun removeNoteFromNoteList(
        boardId: String,
        noteListId: String,
        noteId: String,
    ) {
        auth.currentUser ?: throw Exception("User not logged in")

        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)
        val noteRef = noteListRef.collection("notes").document(noteId)

        try {
            noteRef.delete().await()
            noteNotFound(boardId, noteListId, noteId)
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                noteNotFound(boardId, noteListId, noteId)
                throw Exception("Note not found")
            } else if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) { //Only occur when you is not a member
                noteNotFound(boardId, noteListId, noteId)
                boardNotFound(boardId)
                throw Exception("Board not found or missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    override suspend fun archiveCompletedNotesInList(boardId: String, noteListId: String) {
        auth.currentUser ?: throw Exception("User not logged in")

        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)
        val notesRef = noteListRef.collection("notes")

        try {
            val snapshot = notesRef
                .whereEqualTo("completed", true)
                .whereNotEqualTo("archived", true)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val batch = firestore.batch()
                snapshot.documents.forEach {
                    batch.update(it.reference, "archived", true)
                }
                batch.commit().await()
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) { //Only occur when you is not a member
                boardNotFound(boardId)
                throw Exception("Board not found or missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    override suspend fun archiveAllNotesInList(boardId: String, noteListId: String) {
        auth.currentUser ?: throw Exception("User not logged in")

        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)
        val notesRef = noteListRef.collection("notes")

        try {
            val snapshot = notesRef
                .whereNotEqualTo("archived", true)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val batch = firestore.batch()
                snapshot.documents.forEach {
                    batch.update(it.reference, "archived", true)
                }
                batch.commit().await()
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) { //Only occur when you is not a member
                boardNotFound(boardId)
                throw Exception("Board not found or missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    override suspend fun deleteAllNotesInList(boardId: String, noteListId: String) {
        auth.currentUser ?: throw Exception("User not logged in")

        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)
        val notesRef = noteListRef.collection("notes")

        try {
            val snapshot = notesRef
                .whereNotEqualTo("archived", true)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val batch = firestore.batch()
                snapshot.documents.forEach {
                    batch.delete(it.reference)
                }
                batch.commit().await()
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) { //Only occur when you is not a member
                boardNotFound(boardId)
                throw Exception("Board not found or missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    // *OK
    override suspend fun getNote(boardId: String, noteListId: String, noteId: String): Note {
        auth.currentUser ?: throw Exception("User not logged in")

        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)
        val noteRef = noteListRef.collection("notes").document(noteId)

        try {
            val noteSnapshot = noteRef.get().await()
            if (!noteSnapshot.exists()) throw Exception("Note not found")
            val note =
                noteSnapshot.toObject(Note::class.java) ?: throw Exception("Invalid note type")

            val (checklistsSnapshot, commentsSnapshot) = coroutineScope {
                val checklistTask = async { noteRef.collection("checklists").get().await() }
                val commentTask = async {
                    noteRef.collection("comments").orderBy("createdAt", Query.Direction.DESCENDING)
                        .get().await()
                }
                Pair(checklistTask.await(), commentTask.await())
            }
            val checklists = coroutineScope {
                checklistsSnapshot.documents.map { checklistDoc ->
                    async {
                        try {
                            val checklist = checklistDoc.toObject(Checklist::class.java)
                            val tasksSnapshot =
                                checklistDoc.reference.collection("tasks").get().await()
                            val tasks = tasksSnapshot.documents.mapNotNull {
                                it.toObject(Task::class.java)
                            }
                            checklist?.copy(tasks = tasks)
                        } catch(_: Exception) {
                            null
                        }
                    }
                }.awaitAll().filterNotNull()
            }
            val comments = commentsSnapshot.documents.mapNotNull {
                it.toObject(Comment::class.java)
            }

            val result = note.copy(checklists = checklists, comments = comments)
            _currentNote.value = result
            return result
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) { //Only occur when you is not a member
                boardNotFound(boardId)
                throw Exception("Board not found or missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    // *OK
    override suspend fun updateNoteName(
        boardId: String,
        noteListId: String,
        noteId: String,
        name: String,
    ) {
        auth.currentUser ?: throw Exception("User not logged in")

        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)
        val noteRef = noteListRef.collection("notes").document(noteId)

        try {
            noteRef.update("name", name).await()

            if (_currentNote.value?.docId == noteId) {
                _currentNote.update { it?.copy(name = name) }
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                noteNotFound(boardId, noteListId, noteId)
                throw Exception("Note not found")
            } else if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) { //Only occur when you is not a member
                noteNotFound(boardId, noteListId, noteId)
                boardNotFound(boardId)
                throw Exception("Board not found or missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    // *OK
    override suspend fun updateNoteCover(
        boardId: String,
        noteListId: String,
        noteId: String,
        color: Int?,
    ) {
        auth.currentUser ?: throw Exception("User not logged in")

        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)
        val noteRef = noteListRef.collection("notes").document(noteId)

        try {
            noteRef.update("cover", color).await()

            if (_currentNote.value?.docId == noteId) {
                _currentNote.update { it?.copy(cover = color) }
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                noteNotFound(boardId, noteListId, noteId)
                throw Exception("Note not found")
            } else if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) { //Only occur when you is not a member
                noteNotFound(boardId, noteListId, noteId)
                boardNotFound(boardId)
                throw Exception("Board not found or missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    // *OK
    override suspend fun updateNoteDescription(
        boardId: String,
        noteListId: String,
        noteId: String,
        description: String,
    ) {
        auth.currentUser ?: throw Exception("User not logged in")

        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)
        val noteRef = noteListRef.collection("notes").document(noteId)

        try {
            noteRef.update("description", description).await()

            if (_currentNote.value?.docId == noteId) {
                _currentNote.update { it?.copy(description = description) }
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                noteNotFound(boardId, noteListId, noteId)
                throw Exception("Note not found")
            } else if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) { //Only occur when you is not a member
                noteNotFound(boardId, noteListId, noteId)
                boardNotFound(boardId)
                throw Exception("Board not found or missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    // *OK
    override suspend fun updateNoteComplete(
        boardId: String,
        noteListId: String,
        noteId: String,
        newState: Boolean,
    ) {
        auth.currentUser ?: throw Exception("User not logged in")

        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)
        val noteRef = noteListRef.collection("notes").document(noteId)

        try {
            noteRef.update("completed", newState).await()

            if (_currentNote.value?.docId == noteId) {
                _currentNote.update { it?.copy(completed = newState) }
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                noteNotFound(boardId, noteListId, noteId)
                throw Exception("Note not found")
            } else if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) { //Only occur when you is not a member
                noteNotFound(boardId, noteListId, noteId)
                boardNotFound(boardId)
                throw Exception("Board not found or missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    // *OK
    override suspend fun updateNoteArchive(
        boardId: String,
        noteListId: String,
        noteId: String,
        newState: Boolean,
    ) {
        auth.currentUser ?: throw Exception("User not logged in")

        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)
        val noteRef = noteListRef.collection("notes").document(noteId)

        try {
            noteRef.update("archived", newState).await()

            if (_currentNote.value?.docId == noteId) {
                _currentNote.update { it?.copy(archived = newState) }
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                noteNotFound(boardId, noteListId, noteId)
                throw Exception("Note not found")
            } else if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) { //Only occur when you is not a member
                noteNotFound(boardId, noteListId, noteId)
                boardNotFound(boardId)
                throw Exception("Board not found or missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    // *OK
    override suspend fun updateNoteStartDate(
        boardId: String,
        noteListId: String,
        noteId: String,
        dateTime: Timestamp?,
    ) {
        auth.currentUser ?: throw Exception("User not logged in")

        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)
        val noteRef = noteListRef.collection("notes").document(noteId)

        try {
            noteRef.update("startDate", dateTime).await()

            if (_currentNote.value?.docId == noteId) {
                _currentNote.update { it?.copy(startDate = dateTime) }
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                noteNotFound(boardId, noteListId, noteId)
                throw Exception("Note not found")
            } else if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) { //Only occur when you is not a member
                noteNotFound(boardId, noteListId, noteId)
                boardNotFound(boardId)
                throw Exception("Board not found or missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    // *OK
    override suspend fun updateNoteEndDate(
        boardId: String,
        noteListId: String,
        noteId: String,
        dateTime: Timestamp?,
    ) {
        auth.currentUser ?: throw Exception("User not logged in")

        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)
        val noteRef = noteListRef.collection("notes").document(noteId)

        try {
            noteRef.update("endDate", dateTime).await()

            if (_currentNote.value?.docId == noteId) {
                _currentNote.update { it?.copy(endDate = dateTime) }
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                noteNotFound(boardId, noteListId, noteId)
                throw Exception("Note not found")
            } else if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) { //Only occur when you is not a member
                noteNotFound(boardId, noteListId, noteId)
                boardNotFound(boardId)
                throw Exception("Board not found or missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    override suspend fun addNewChecklist(
        boardId: String,
        noteListId: String,
        noteId: String,
        checklist: Checklist,
    ) {
        auth.currentUser ?: throw Exception("User not logged in")

        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)
        val noteRef = noteListRef.collection("notes").document(noteId)
        val checklistRef = noteRef.collection("checklists").document()

        val newChecklist = checklist.copy(docId = checklistRef.id)
        try {
            firestore.runTransaction { transaction ->
                val noteSnapshot = transaction.get(noteRef)
                if (!noteSnapshot.exists()) {
                    noteNotFound(boardId, noteListId, noteId)
                    throw Exception("Note not found")
                }
                transaction.set(checklistRef, newChecklist)
            }.await()
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) { //Only occur when you is not a member
                noteNotFound(boardId, noteListId, noteId)
                boardNotFound(boardId)
                throw Exception("Board not found or missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    override suspend fun deleteChecklist(
        boardId: String,
        noteListId: String,
        noteId: String,
        checklistId: String,
    ) {
        auth.currentUser ?: throw Exception("User not logged in")

        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)
        val noteRef = noteListRef.collection("notes").document(noteId)
        val checklistRef = noteRef.collection("checklists").document(checklistId)

        try {
            firestore.runTransaction { transaction ->
                val noteSnapshot = transaction.get(noteRef)
                if (!noteSnapshot.exists()) {
                    noteNotFound(boardId, noteListId, noteId)
                    throw Exception("Note not found")
                }
                transaction.delete(checklistRef)
            }.await()

            if (_currentNote.value?.docId == noteId) {
                _currentNote.update { note ->
                    val currentChecklists = note?.checklists ?: emptyList()
                    note?.copy(checklists = currentChecklists.filterNot { it.docId == checklistId })
                }
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) { //Only occur when you is not a member
                noteNotFound(boardId, noteListId, noteId)
                boardNotFound(boardId)
                throw Exception("Board not found or missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    override suspend fun updateChecklistName(
        boardId: String,
        noteListId: String,
        noteId: String,
        checklistId: String,
        name: String,
    ) {
        auth.currentUser ?: throw Exception("User not logged in")

        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)
        val noteRef = noteListRef.collection("notes").document(noteId)
        val checklistRef = noteRef.collection("checklists").document(checklistId)

        try {
            firestore.runTransaction { transaction ->
                val noteSnapshot = transaction.get(noteRef)
                if (!noteSnapshot.exists()) {
                    noteNotFound(boardId, noteListId, noteId)
                    throw Exception("Note not found")
                }
                transaction.update(checklistRef, "name", name)
            }.await()
            if (_currentNote.value?.docId == noteId) {
                _currentNote.update { note ->
                    val currentChecklists = (note?.checklists ?: emptyList()).map {
                        if (it.docId == checklistId) it.copy(name = name) else it
                    }
                    note?.copy(checklists = currentChecklists)
                }
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                checklistNotFound(boardId, noteListId, noteId, checklistId)
                throw Exception("Checklist not found")
            } else if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) { //Only occur when you is not a member
                noteNotFound(boardId, noteListId, noteId)
                boardNotFound(boardId)
                throw Exception("Board not found or missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    override suspend fun addNewTask(
        boardId: String,
        noteListId: String,
        noteId: String,
        checklistId: String,
        task: Task,
    ) {
        auth.currentUser ?: throw Exception("User not logged in")

        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)
        val noteRef = noteListRef.collection("notes").document(noteId)
        val checklistRef = noteRef.collection("checklists").document(checklistId)
        val taskRef = checklistRef.collection("tasks").document()
        val newTask = task.copy(docId = taskRef.id)

        try {
            firestore.runTransaction { transaction ->
                val noteSnapshot = transaction.get(noteRef)
                if (!noteSnapshot.exists()) {
                    noteNotFound(boardId, noteListId, noteId)
                    throw Exception("Note not found")
                }
                val checklistSnapshot = transaction.get(checklistRef)
                if (!checklistSnapshot.exists()) {
                    checklistNotFound(boardId, noteListId, noteId, checklistId)
                    throw Exception("Checklist not found when add task")
                }
                transaction.set(taskRef, newTask)
            }.await()
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                checklistNotFound(boardId, noteListId, noteId, checklistId)
                throw Exception("Checklist not found")
            } else if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) { //Only occur when you is not a member
                noteNotFound(boardId, noteListId, noteId)
                boardNotFound(boardId)
                throw Exception("Board not found or missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    override suspend fun deleteTask(
        boardId: String,
        noteListId: String,
        noteId: String,
        checklistId: String,
        taskId: String,
    ) {
        auth.currentUser ?: throw Exception("User not logged in")

        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)
        val noteRef = noteListRef.collection("notes").document(noteId)
        val checklistRef = noteRef.collection("checklists").document(checklistId)
        val taskRef = checklistRef.collection("tasks").document(taskId)

        try {
            firestore.runTransaction { transaction ->
                val noteSnapshot = transaction.get(noteRef)
                if (!noteSnapshot.exists()) {
                    noteNotFound(boardId, noteListId, noteId)
                    throw Exception("Note not found")
                }
                val checklistSnapshot = transaction.get(checklistRef)
                if (!checklistSnapshot.exists()) {
                    checklistNotFound(boardId, noteListId, noteId, checklistId)
                    throw Exception("Checklist not found when delete task")
                }
                transaction.delete(taskRef)
            }.await()
            if (_currentNote.value?.docId == noteId) {
                _currentNote.update { note ->
                    note?.copy(
                        checklists = note.checklists.map { checklist ->
                            if (checklist.docId == checklistId) {
                                checklist.copy(
                                    tasks = checklist.tasks.filterNot { it.docId == taskId }
                                )
                            } else checklist
                        }
                    )
                }
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) { //Only occur when you is not a member
                noteNotFound(boardId, noteListId, noteId)
                boardNotFound(boardId)
                throw Exception("Board not found or missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    override suspend fun updateTaskName(
        boardId: String,
        noteListId: String,
        noteId: String,
        checklistId: String,
        taskId: String,
        name: String,
    ) {
        auth.currentUser ?: throw Exception("User not logged in")

        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)
        val noteRef = noteListRef.collection("notes").document(noteId)
        val checklistRef = noteRef.collection("checklists").document(checklistId)
        val taskRef = checklistRef.collection("tasks").document(taskId)

        try {
            firestore.runTransaction { transaction ->
                val noteSnapshot = transaction.get(noteRef)
                if (!noteSnapshot.exists()) {
                    noteNotFound(boardId, noteListId, noteId)
                    throw Exception("Note not found")
                }
                val checklistSnapshot = transaction.get(checklistRef)
                if (!checklistSnapshot.exists()) {
                    checklistNotFound(boardId, noteListId, noteId, checklistId)
                    throw Exception("Checklist not found when update task name")
                }
                transaction.update(taskRef, "name", name)
            }.await()
            if (_currentNote.value?.docId == noteId) {
                _currentNote.update { note ->
                    note?.copy(
                        checklists = note.checklists.map { checklist ->
                            if (checklist.docId == checklistId) {
                                checklist.copy(
                                    tasks = checklist.tasks.map { task ->
                                        if (task.docId == taskId) {
                                            task.copy(name = name)
                                        } else task
                                    }
                                )
                            } else checklist
                        }
                    )
                }
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                taskNotFound(boardId, noteListId, noteId, checklistId, taskId)
                throw Exception("Checklist not found")
            } else if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) { //Only occur when you is not a member
                noteNotFound(boardId, noteListId, noteId)
                boardNotFound(boardId)
                throw Exception("Board not found or missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    override suspend fun updateTaskDone(
        boardId: String,
        noteListId: String,
        noteId: String,
        checklistId: String,
        taskId: String,
        done: Boolean,
    ) {
        auth.currentUser ?: throw Exception("User not logged in")

        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)
        val noteRef = noteListRef.collection("notes").document(noteId)
        val checklistRef = noteRef.collection("checklists").document(checklistId)
        val taskRef = checklistRef.collection("tasks").document(taskId)

        try {
            firestore.runTransaction { transaction ->
                val noteSnapshot = transaction.get(noteRef)
                if (!noteSnapshot.exists()) {
                    noteNotFound(boardId, noteListId, noteId)
                    throw Exception("Note not found")
                }
                val checklistSnapshot = transaction.get(checklistRef)
                if (!checklistSnapshot.exists()) {
                    checklistNotFound(boardId, noteListId, noteId, checklistId)
                    throw Exception("Checklist not found when update task done")
                }
                transaction.update(taskRef, "done", done)
            }.await()
            if (_currentNote.value?.docId == noteId) {
                _currentNote.update { note ->
                    note?.copy(
                        checklists = note.checklists.map { checklist ->
                            if (checklist.docId == checklistId) {
                                checklist.copy(
                                    tasks = checklist.tasks.map { task ->
                                        if (task.docId == taskId) {
                                            task.copy(done = done)
                                        } else task
                                    }
                                )
                            } else checklist
                        }
                    )
                }
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                taskNotFound(boardId, noteListId, noteId, checklistId, taskId)
                throw Exception("Checklist not found")
            } else if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) { //Only occur when you is not a member
                noteNotFound(boardId, noteListId, noteId)
                boardNotFound(boardId)
                throw Exception("Board not found or missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    override suspend fun addComment(
        boardId: String,
        noteListId: String,
        noteId: String,
        comment: Comment,
    ) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)
        val noteRef = noteListRef.collection("notes").document(noteId)
        val commentRef = noteRef.collection("comments").document()
        val newComment = comment.copy(
            docId = commentRef.id,
            createdBy = authUser.uid,
            createdAt = Timestamp.now()
        )

        try {
            firestore.runTransaction { transaction ->
                val noteSnapshot = transaction.get(noteRef)
                if (!noteSnapshot.exists()) {
                    noteNotFound(boardId, noteListId, noteId)
                    throw Exception("Note not found")
                }
                transaction.set(commentRef, newComment)
            }.await()
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) { //Only occur when you is not a member
                noteNotFound(boardId, noteListId, noteId)
                boardNotFound(boardId)
                throw Exception("Board not found or missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    override suspend fun deleteComment(
        boardId: String,
        noteListId: String,
        noteId: String,
        commentId: String,
    ) {
        auth.currentUser ?: throw Exception("User not logged in")

        val boardRef = firestore.collection("boards").document(boardId)
        val noteListRef = boardRef.collection("notelists").document(noteListId)
        val noteRef = noteListRef.collection("notes").document(noteId)
        val commentRef = noteRef.collection("comments").document(commentId)

        try {
            firestore.runTransaction { transaction ->
                val noteSnapshot = transaction.get(noteRef)
                if (!noteSnapshot.exists()) {
                    noteNotFound(boardId, noteListId, noteId)
                    throw Exception("Note not found")
                }
                transaction.delete(commentRef)
            }.await()

            if (_currentNote.value?.docId == noteId) {
                _currentNote.update { note ->
                    val currentComments = note?.comments ?: emptyList()
                    note?.copy(comments = currentComments.filterNot { it.docId == commentId })
                }
            }
        } catch(e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) { //Only occur when you is not a member
                noteNotFound(boardId, noteListId, noteId)
                boardNotFound(boardId)
                throw Exception("Board not found or missing permission to perform this action")
            }
            throw e
        } catch(e: Exception) {
            throw e
        }
    }

    // *OK
    override fun clearCache() {
        repoScope.coroutineContext.cancelChildren()
        removeBoardsListener()
        removeCurrentBoardListener()
        _currentNote.value = null
        _currentBoard.value = null
        _boards.value = emptyList()
    }
}

package com.apcs.worknestapp.ui.screens.board

import android.util.Log
import androidx.compose.animation.core.copy
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavHostController
import com.apcs.worknestapp.data.remote.board.BoardViewModel
import com.apcs.worknestapp.data.remote.board.Notelist
import com.apcs.worknestapp.data.remote.note.Note

import com.apcs.worknestapp.ui.components.board.BoardActionDropdownMenu
import com.apcs.worknestapp.ui.components.board.NoteListCard
import com.apcs.worknestapp.ui.screens.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardScreen(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    boardViewModel: BoardViewModel = hiltViewModel(),
    boardId: String?
) {
    val coroutineScope = rememberCoroutineScope()
    var isFirstLoad by rememberSaveable { mutableStateOf(true) }

    val currentBoardState by boardViewModel.boards.collectAsState()
    val board = remember(currentBoardState, boardId) {
        currentBoardState.find { it.docId == boardId }
    }

    val notelists by remember(boardId) {
        boardViewModel.getNotelistsForBoard(boardId)
    }.collectAsState(initial = emptyList())

    var editableBoardName by remember(board?.name) { mutableStateOf(board?.name ?: "") }
    LaunchedEffect(board?.name) {
        if (board?.name != editableBoardName) {
            editableBoardName = board?.name ?: ""
        }
    }

    val focusManager = LocalFocusManager.current
    var isEditingBoardName by remember { mutableStateOf(false) }


    LaunchedEffect(boardId) {
        if (boardId != null) {
            if (isFirstLoad) {
                boardViewModel.refreshNotelists(boardId)
                isFirstLoad = false
            }
        }
    }

    LifecycleResumeEffect(key1 = boardId) {
        boardViewModel.registerBoardListener()
        if (boardId != null) {
            boardViewModel.registerNotelistListener(boardId)
        }
        onPauseOrDispose {
            boardViewModel.removeBoardListener()
            if (boardId != null) {
                boardViewModel.removeNotelistListener()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (board != null) {
                        BasicTextField(
                            value = editableBoardName,
                            onValueChange = { editableBoardName = it },
                            textStyle = MaterialTheme.typography.titleLarge.copy( // Or titleMedium
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (editableBoardName.isNotBlank() && editableBoardName != board.name) {
                                        boardViewModel.updateBoardName(board.docId!!, editableBoardName)
                                    }
                                    isEditingBoardName = false
                                    focusManager.clearFocus()
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 8.dp)
                                .onFocusChanged { focusState ->
                                    isEditingBoardName = focusState.isFocused
                                    if (!focusState.isFocused && editableBoardName.isNotBlank() && editableBoardName != board.name) {
                                    } else if (!focusState.isFocused && editableBoardName.isNotBlank() && editableBoardName == board.name) {
                                    } else if (!focusState.isFocused && editableBoardName.isBlank()) {
                                        editableBoardName = board.name ?: ""
                                    }
                                }
                                .onKeyEvent { keyEvent ->
                                    if(keyEvent.key == Key.Enter) {
                                        if (editableBoardName.isNotBlank() && editableBoardName != board.name) {
                                            boardViewModel.updateBoardName(
                                                board.docId!!,
                                                editableBoardName
                                            )
                                        }
                                        isEditingBoardName = false
                                        focusManager.clearFocus()
                                        true // Consume the event
                                    } else {
                                        false
                                    }
                                },
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                        )
                    } else {
                        Text("Loading Board...") // Fallback while board is null
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (board != null) { // Only show actions if board is loaded
                        var menuExpanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { menuExpanded = !menuExpanded }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Board Actions"
                            )
                        }
                        BoardActionDropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            // Pass appropriate actions for the board context
                            onRenameBoard = {
                                menuExpanded = false
                                // Could also trigger focus on the BasicTextField here
                                isEditingBoardName = true // Maybe set focus to BasicTextField
                            },
                            onDeleteBoard = {
                                menuExpanded = false
                                if (board?.docId != null) {
                                    coroutineScope.launch {
                                        try {
                                            val success = boardViewModel.deleteBoard(board.docId!!)
                                            if (success) {
                                                navController.popBackStack()
                                            } else {
                                                snackbarHostState.showSnackbar(
                                                    message = "Failed to delete board. Please try again.",
                                                    withDismissAction = true
                                                )
                                            }
                                        } catch (e: Exception) {
                                            Log.e("BoardScreen", "Error deleting board: ${board.docId}", e)
                                            snackbarHostState.showSnackbar(
                                                message = "An error occurred while deleting the board.",
                                                withDismissAction = true
                                            )
                                        }
                                    }
                                } else {
                                    Log.w("BoardScreen", "Attempted to delete a board with a null ID.")
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Cannot delete board: Board ID is missing.",
                                            withDismissAction = true
                                        )
                                    }
                                }
                            },
                            onManageMembers = {
                                // TODO: manage members
                            },
                            onChangeCover = {
                            // TODO: color picker
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = if (isEditingBoardName) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent, // Change background when editing
                    scrolledContainerColor = MaterialTheme.colorScheme.surface // Or your preference
                )
            )
        },
        modifier = modifier
            .fillMaxSize()
            .background(
                color = board?.cover?.let { Color(it).copy(alpha = 0.1f) }
                    ?: MaterialTheme.colorScheme.surfaceVariant
            )
    ) { innerPadding ->
        if (board == null && boardId != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
                Text("Loading board details...")
            }
            return@Scaffold
        } else if (board == null && boardId == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Board not found or ID missing.")
            }
            return@Scaffold
        }


        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        board?.docId?.let { currentBoardDocId ->
                            val newNotelist = Notelist(name = "New List")
                            boardViewModel.addNotelist(currentBoardDocId, newNotelist)
                        }
                    }
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.End)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Note List")
                Spacer(Modifier.width(8.dp))
                Text("Add New List")
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = notelists,
                    key = { notelist -> notelist.docId ?: notelist.name.hashCode().toString() }
                ) { notelist ->
                    NoteListCard(
                        boardViewModel = boardViewModel,
                        boardId = boardId!!,
                        notelist = notelist,
                        onAddNoteClick = { listId, newNoteName ->
                            coroutineScope.launch {
                                val newNote = Note(name = newNoteName, createdAt = com.google.firebase.Timestamp.now())
                                boardViewModel.addNoteToList(boardId!!,listId, newNote)
                            }
                        },
                        onNoteClick = { note ->
                            if (note.docId != null) {
                                navController.navigate(
                                    "board_note_detail/${boardId}/${notelist.docId}/${note.docId}"
                                )
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Cannot open note: Note ID is missing.",
                                        withDismissAction = true
                                    )
                                }
                            }
                        },
                        onNoteCheckedChange = { note, isChecked ->
                            boardViewModel.updateNoteCheckedStatus(boardId!!, notelist.docId!!, note.docId!!, isChecked)
                        },
                        onRemoveNotelist = {
                            coroutineScope.launch {
                                boardViewModel.removeNotelist(boardId!!,notelist.docId!!)
                            }
                        },
                        onRemoveSpecificNote = { listId, noteId ->
                            coroutineScope.launch {
                                boardViewModel.removeNoteFromNotelist(boardId!!,listId, noteId)
                            }
                        },
                        onUpdateNotelistName = { boardId, notelistId, newName ->
                            coroutineScope.launch {
                                boardViewModel.updateNotelistName(boardId, notelistId, newName)
                            }
                        },
                        modifier = Modifier
                            .width(300.dp)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}
package com.apcs.worknestapp.ui.screens.board

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavHostController
import com.apcs.worknestapp.data.remote.board.BoardViewModel
import com.apcs.worknestapp.data.remote.board.Notelist
import com.apcs.worknestapp.data.remote.note.Note

import com.apcs.worknestapp.ui.components.board.BoardActionDropdownMenu
import com.apcs.worknestapp.ui.components.board.NoteListCard
import com.apcs.worknestapp.ui.components.topbar.CustomTopBar
import kotlinx.coroutines.launch

// Dummy data classes for placeholder UI elements
//enum class BoardSortOption { NAME, }
//data class BoardFilterOption(val id: String, val name: String, var isSelected: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardScreen2(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    boardViewModel: BoardViewModel = hiltViewModel(),
    boardId: String?
) {
    val coroutineScope = rememberCoroutineScope()
    var isFirstLoad by rememberSaveable { mutableStateOf(true) }

    val currentBoardState = boardViewModel.boards.collectAsState()
    val board = remember(currentBoardState.value, boardId) {
        currentBoardState.value.find { it.docId == boardId }
    }

    val notelists by remember(boardId) {
        boardViewModel.getNotelistsForBoard(boardId)
    }.collectAsState(initial = emptyList())


    LaunchedEffect(boardId) {
        if (boardId != null) {
            if (isFirstLoad) {
                boardViewModel.refreshBoardsIfEmpty()
                boardViewModel.refreshNotelists(boardId)
                isFirstLoad = false
            }
        }
    }

    LifecycleResumeEffect(Unit) {
        boardViewModel.registerListener()
        // Assuming a listener for notelists as well
        boardViewModel.registerNotelistListener(boardId)
        onPauseOrDispose {
            boardViewModel.removeListener()
            boardViewModel.removeNotelistListener()
        }
    }

    Scaffold(
        topBar = {
            if (board != null) {
                CustomTopBar(
                    field = board.name ?: "Board",
                    showDivider = true,
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // TODO: Implement notifications, menu actions
                        // ...
                    }
                )
            } else {
                CustomTopBar(field = "Loading Board...")
            }
        },
        modifier = modifier
            .fillMaxSize()
            .background(
                color = board?.cover?.let { Color(it) } ?: MaterialTheme.colorScheme.background
            )
    ) { innerPadding ->
        if (board == null) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
                Text("Loading board details or board not found...")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Button to add a new notelist
            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        val newNotelist = Notelist(name = "New List")
                        boardViewModel.addNotelist(board.docId!!, newNotelist)
                    }
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.End) // Align to the right
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
                    key = { notelist -> notelist.docId ?: notelist.name.hashCode() }
                ) { notelist ->
                    NoteListCard(
                        notelist = notelist,
                        onAddNoteClick = { newNoteName ->
                            coroutineScope.launch {
                                // Calls the ViewModel function to add a note to the specific notelist
                                val newNote = Note(name = newNoteName)
                                boardViewModel.addNoteToList(notelist.docId!!, newNote)
                            }
                        },
                        onNoteClick = { note ->
                            // TODO: Handle clicking on a note (e.g., open note details)
                        },
                        onNoteCheckedChange = { note, isChecked ->
                            // TODO: Handle note completion status change
                        },
                        onRemoveNotelist = {
                            coroutineScope.launch {
                                // Calls the ViewModel function to remove the entire notelist
                                boardViewModel.removeNotelist(notelist.docId!!)
                            }
                        },
                        onRemoveNote = { noteId ->
                            coroutineScope.launch {
                                // Calls the ViewModel function to remove a single note from the notelist
                                boardViewModel.removeNoteFromNotelist(notelist.docId!!, noteId)
                            }
                        },
                        modifier = Modifier.width(300.dp)
                    )
                }
            }
        }
    }
}

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


    val currentBoardState = boardViewModel.boards.collectAsState()

    val board = remember(currentBoardState.value, boardId) {
        currentBoardState.value.find { it.docId == boardId }
    }

    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var showBoardActionMenu by remember { mutableStateOf(false) }

    //var currentSortOption by rememberSaveable { mutableStateOf(BoardSortOption.NAME) }
    //val filterOptions = remember {
    //    mutableStateListOf(
    //        BoardFilterOption("filter1", "Active", false),
    //        BoardFilterOption("filter2", "Archived", false)
    //    )
    //}

    LaunchedEffect(boardId) {
        if (boardId != null) {
            if (isFirstLoad) {
                boardViewModel.refreshBoardsIfEmpty()
                isFirstLoad = false
            }
        }
    }

    LifecycleResumeEffect(Unit) {
        boardViewModel.registerListener()
        onPauseOrDispose {
            boardViewModel.removeListener()
        }
    }

    Scaffold(
        topBar = {
            if (board != null) {
                CustomTopBar(
                    field = board.name ?: "Board",
                    showDivider = true,
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {///popBackStack???
                             Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                         }
                    },
                    actions = {
                        IconButton(onClick = {
                            // TODO: Handle notifications click
                        }) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                        }

                        IconButton(onClick = { showBoardActionMenu = true }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Board Menu")
                        }
                        BoardActionDropdownMenu(
                            expanded = showBoardActionMenu,
                            onDismissRequest = { showBoardActionMenu = false },
                            onRenameBoard = { /* TODO */ },
                            onChangeCover = { /* TODO */ },
                            onManageMembers = { /* TODO */ },
                            onDeleteBoard = { /* TODO */ }
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            } else {
                CustomTopBar(field = "Loading Board...")
            }
        },
        modifier = modifier
            .fillMaxSize()
            .background(
                color = board?.cover?.let { Color(it) } ?: MaterialTheme.colorScheme.background
            )
    ) { innerPadding ->
        if (board == null) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
                Text("Loading board details or board not found...")
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
                    // TODO: Handle adding a new notelist to the board
                    // Example: boardViewModel.addNotelistToBoard(board.docId, newNotelistName)
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.Start) // Or Alignment.End depending on where you want it
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Note List")
                Spacer(Modifier.width(8.dp))
                Text("Add New List")
            }

            // Horizontally scrollable row of NoteLists
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Takes remaining space
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(items = board.notelists, key = { notelist -> notelist.docId ?: notelist.name.hashCode() }) { notelist ->
                    NoteListCard(
                        notelist = notelist,
                        onAddNoteClick = { listId, noteName ->
                            // TODO: Handle adding a new note to this notelist
                            // boardViewModel.addNoteToNotelist(board.docId, listId, newNote)
                        },
                        onNoteClick = { note ->
                            // TODO: Handle clicking on a note (e.g., open note details)
                        },
                        onNoteCheckedChange = { note, isChecked ->
                            // TODO: Handle note completion status change
                            // boardViewModel.updateNoteCompletion(board.docId, notelist.docId, note.docId, isChecked)
                        },
                        modifier = Modifier.width(300.dp) // Example width for each notelist card
                    )
                }
            }
        }
    }
}


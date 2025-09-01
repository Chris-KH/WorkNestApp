package com.apcs.worknestapp.ui.screens.board

import androidx.compose.foundation.background
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
                        onAddNoteClick = { listId, newNoteName ->
                            coroutineScope.launch {
                                val newNote = Note(name = newNoteName)
                                boardViewModel.addNoteToList(
                                    notelist.docId!!,
                                    newNote
                                ) // Or use notelist.docId if it's guaranteed non-null
                            }
                        },
                        onNoteClick = { note ->
                            // TODO: Handle clicking on a note (e.g., open note details)
                            println("Note clicked: ${note.name}")
                        },
                        onNoteCheckedChange = { note, isChecked ->
                            // TODO: Handle note completion status change
                            println("Note checked: ${note.name}, isChecked: $isChecked")
                            // Example:
                            // coroutineScope.launch {
                            //     boardViewModel.updateNoteCheckedStatus(note.docId!!, isChecked)
                            // }
                        },
                        onRemoveNotelist = {
                            coroutineScope.launch {
                                boardViewModel.removeNotelist(notelist.docId!!)
                            }
                        },
                        onRemoveSpecificNote = { listId, noteId ->
                            coroutineScope.launch {
                                boardViewModel.removeNoteFromNotelist(listId, noteId)
                            }
                        },
                        modifier = Modifier.width(300.dp)
                    )
                }
            }
        }
    }
}


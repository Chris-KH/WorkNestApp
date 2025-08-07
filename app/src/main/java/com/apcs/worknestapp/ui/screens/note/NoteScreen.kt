package com.apcs.worknestapp.ui.screens.note

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavHostController
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.note.Note
import com.apcs.worknestapp.data.remote.note.NoteViewModel
import com.apcs.worknestapp.ui.components.LoadingScreen
import com.apcs.worknestapp.ui.components.bottombar.MainBottomBar
import com.apcs.worknestapp.ui.components.topbar.MainTopBar
import com.apcs.worknestapp.ui.screens.Screen
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
    noteViewModel: NoteViewModel = hiltViewModel(),
) {
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var isFirstLoad by rememberSaveable { mutableStateOf(true) }
    var isInSelectMode by remember { mutableStateOf(false) }
    var showActionMenu by remember { mutableStateOf(false) }
    var shouldShowNoteItemDialog by remember { mutableStateOf<Note?>(null) }

    val notes = noteViewModel.notes.collectAsState()
    var notesSortBy by rememberSaveable { mutableStateOf(NoteSortBy.NEWEST) }
    val displayNotes = notes.value
        .filterNot { it.archived == true }
        .let { list ->
            when(notesSortBy) {
                NoteSortBy.NEWEST -> list.sortedByDescending { it.createdAt }
                NoteSortBy.OLDEST -> list.sortedBy { it.createdAt }
                NoteSortBy.ALPHABETICAL -> list.sortedBy { it.name }
            }
        }

    LaunchedEffect(Unit) {
        if (isFirstLoad && notes.value.isEmpty()) {
            noteViewModel.refreshNotesIfEmpty()
        }
        isFirstLoad = false
    }

    LifecycleResumeEffect(Unit) {
        noteViewModel.registerListener()
        onPauseOrDispose {
            noteViewModel.removeListener()
        }
    }

    Scaffold(
        topBar = {
            MainTopBar(
                title = if (isInSelectMode) "Select notes" else Screen.Note.title,
                actions = {
                    if (displayNotes.isNotEmpty()) {
                        IconButton(
                            onClick = { isInSelectMode = !isInSelectMode },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = if (isInSelectMode) MaterialTheme.colorScheme.surface
                                else MaterialTheme.colorScheme.primary,
                                disabledContentColor = Color.Unspecified,
                            )
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_select),
                                contentDescription = "Select mode",
                                modifier = Modifier
                                    .size(24.dp)
                                    .zIndex(10f)
                            )
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isInSelectMode) MaterialTheme.colorScheme.primary
                                        else Color.Unspecified,
                                        shape = CircleShape,
                                    )
                                    .size(36.dp)
                                    .zIndex(1f)
                            )
                        }
                    }
                    IconButton(
                        enabled = !showActionMenu,
                        onClick = { showActionMenu = true },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.symbol_three_dot),
                            contentDescription = "More options",
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(-90f)
                        )
                        NoteDropdownActions(
                            expanded = showActionMenu,
                            onDismissRequest = { showActionMenu = false },
                            onChangeBackground = {},
                            onSort = {},
                            onViewArchive = {},
                            onArchiveCompletedNotes = {
                                coroutineScope.launch {
                                    val isSuccess = noteViewModel.archiveCompletedNotes()
                                    if (!isSuccess) {
                                        snackbarHost.showSnackbar(
                                            message = "Archive completed notes failed",
                                            withDismissAction = true,
                                        )
                                    }
                                    showActionMenu = false
                                }
                            },
                            onArchiveAllNotes = {
                                coroutineScope.launch {
                                    val isSuccess = noteViewModel.archiveAllNotes()
                                    if (!isSuccess) {
                                        snackbarHost.showSnackbar(
                                            message = "Archive all notes failed",
                                            withDismissAction = true,
                                        )
                                    }
                                    showActionMenu = false
                                }
                            },
                            onDeleteAllNotes = {
                                coroutineScope.launch {
                                    val isSuccess = noteViewModel.deleteAllNotes()
                                    if (!isSuccess) {
                                        snackbarHost.showSnackbar(
                                            message = "Delete all notes failed",
                                            withDismissAction = true,
                                        )
                                    }
                                    showActionMenu = false
                                }
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            MainBottomBar(
                currentScreen = Screen.Note,
                navController = navController,
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        var noteName by remember { mutableStateOf("") }
        val interactionSource = remember { MutableInteractionSource() }
        val isFocused by interactionSource.collectIsFocusedAsState()
        val imePadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

        Column(
            modifier = Modifier
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr)
                )
                .padding(
                    bottom = if (isFocused && imePadding > 0.dp) 0.dp
                    else innerPadding.calculateBottomPadding()
                )
                .imePadding()
                .fillMaxSize(),
        ) {
            if (!isFirstLoad) {
                shouldShowNoteItemDialog?.let {
                    NoteItemDialog(
                        onArchive = {
                            coroutineScope.launch {
                                val isSuccess = noteViewModel.updateNoteArchive(
                                    docId = it.docId!!,
                                    newState = true,
                                )
                                if (!isSuccess) snackbarHost.showSnackbar(
                                    message = "Archive note failed",
                                    withDismissAction = true,
                                )
                                shouldShowNoteItemDialog = null
                            }
                        },
                        onDelete = {
                            coroutineScope.launch {
                                val isSuccess = noteViewModel.deleteNote(it.docId!!)
                                if (!isSuccess) snackbarHost.showSnackbar(
                                    message = "Delete note failed",
                                    withDismissAction = true,
                                )
                                shouldShowNoteItemDialog = null
                            }
                        },
                        onDismissRequest = { shouldShowNoteItemDialog = null },
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    if (displayNotes.isEmpty()) {
                        EmptyNote(modifier = Modifier.fillMaxSize())
                    } else {
                        LazyColumn(
                            modifier = Modifier,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp)
                        ) {
                            itemsIndexed(
                                items = displayNotes,
                                key = { _, note -> note.docId.hashCode() }
                            ) { idx, note ->
                                NoteItem(
                                    note = note,
                                    onClick = {
                                        navController.navigate(
                                            Screen.NoteDetail.route.replace(
                                                "{noteId}",
                                                note.docId ?: ""
                                            )
                                        )
                                    },
                                    onCompleteClick = {
                                        coroutineScope.launch {
                                            if (note.docId != null) {
                                                val currentState = note.completed ?: false
                                                val isSuccess =
                                                    noteViewModel.updateNoteComplete(
                                                        docId = note.docId,
                                                        newState = !currentState,
                                                    )
                                                if (!isSuccess) {
                                                    snackbarHost.showSnackbar(
                                                        message = "Mark note completed failed",
                                                        withDismissAction = true,
                                                    )
                                                }
                                            } else {
                                                snackbarHost.showSnackbar(
                                                    message = "Note not founded",
                                                    withDismissAction = true,
                                                )
                                            }
                                        }
                                    },
                                    onLongClick = { shouldShowNoteItemDialog = note }
                                )
                                if (idx + 1 < displayNotes.size)
                                    Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }

                AddNoteInput(
                    value = noteName,
                    onValueChange = { noteName = it },
                    onAdd = {
                        if (noteName.isNotBlank()) {
                            coroutineScope.launch {
                                noteViewModel.addNote(
                                    Note(
                                        name = noteName, description = "", cover = null,
                                        completed = false, archived = false, isLoading = true,
                                        createdAt = Timestamp.now()
                                    )
                                )
                                noteName = ""
                            }
                        }
                    },
                    onCancel = { focusManager.clearFocus() },
                    isFocused = isFocused,
                    interactionSource = interactionSource,
                )
            } else LoadingScreen()
        }
    }
}

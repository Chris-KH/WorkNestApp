package com.apcs.worknestapp.ui.screens.note

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavHostController
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.note.Note
import com.apcs.worknestapp.data.remote.note.NoteViewModel
import com.apcs.worknestapp.ui.components.ConfirmDialog
import com.apcs.worknestapp.ui.components.ConfirmDialogState
import com.apcs.worknestapp.ui.components.LoadingScreen
import com.apcs.worknestapp.ui.components.bottombar.MainBottomBar
import com.apcs.worknestapp.ui.components.topbar.MainTopBar
import com.apcs.worknestapp.ui.screens.Screen
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    val selectedNotes = remember { mutableStateListOf<String>() }

    var showActionMenu by remember { mutableStateOf(false) }
    var showNoteItemDialog by remember { mutableStateOf<Note?>(null) }
    var dialogState by remember { mutableStateOf<ConfirmDialogState?>(null) }

    val archiveModalSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )
    var showArchiveModal by rememberSaveable { mutableStateOf(false) }

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
    val archiveNotes = notes.value.filter { it.archived == true }

    LaunchedEffect(Unit) {
        if (isFirstLoad) {
            val isSuccess = noteViewModel.refreshNotesIfEmpty()
            if (isSuccess) isFirstLoad = false
        }
    }

    LaunchedEffect(isInSelectMode) {
        if (!isInSelectMode) selectedNotes.clear()
    }

    LaunchedEffect(displayNotes) {
        if (isInSelectMode) {
            selectedNotes.removeAll { noteId ->
                displayNotes.find { it.docId == noteId } == null
            }
        }
    }

    LifecycleResumeEffect(Unit) {
        noteViewModel.registerListener()
        onPauseOrDispose { noteViewModel.removeListener() }
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
                            isNoteEmpty = displayNotes.isEmpty(),
                            onDismissRequest = { showActionMenu = false },
                            onChangeBackground = {},
                            onSort = { notesSortBy = it },
                            onViewArchive = {
                                coroutineScope.launch {
                                    showActionMenu = false
                                    showArchiveModal = true
                                    archiveModalSheetState.show()
                                }
                            },
                            onArchiveCompletedNotes = {
                                noteViewModel.archiveCompletedNotes()
                                showActionMenu = false
                            },
                            onArchiveAllNotes = {
                                noteViewModel.archiveAllNotes(archived = true)
                                showActionMenu = false
                            },
                            onDeleteAllNotes = {
                                showActionMenu = false
                                dialogState = ConfirmDialogState(
                                    title = "Delete all notes",
                                    message = "Are you sure to delete all notes",
                                    confirmText = "Delete",
                                    cancelText = "Cancel",
                                    onConfirm = {
                                        dialogState = null
                                        noteViewModel.deleteAllArchivedNotes(archived = false)
                                    },
                                    onCancel = { dialogState = null }
                                )
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

        dialogState?.let {
            ConfirmDialog(
                title = it.title,
                message = it.message,
                onDismissRequest = { dialogState = null },
                confirmText = it.confirmText,
                cancelText = it.cancelText,
                onConfirm = it.onConfirm,
                onCancel = it.onCancel,
            )
        }

        if (showArchiveModal) {
            ArchiveModalBottomSheet(
                sheetState = archiveModalSheetState,
                onDismissRequest = {
                    coroutineScope.launch {
                        archiveModalSheetState.hide()
                        showArchiveModal = false
                    }
                },
                navController = navController,
                archiveNotes = archiveNotes,
                onRestore = { list ->
                    noteViewModel.archiveNotes(noteIds = list, archived = false)
                }
            )
        }

        Column(
            modifier = Modifier
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr)
                )
                .padding(
                    bottom = if (isFocused && WindowInsets.isImeVisible) 0.dp
                    else innerPadding.calculateBottomPadding()
                )
                .imePadding()
                .fillMaxSize(),
        ) {
            if (!isFirstLoad) {
                showNoteItemDialog?.let {
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
                                showNoteItemDialog = null
                            }
                        },
                        onDelete = {
                            noteViewModel.deleteNote(it.docId!!)
                            showNoteItemDialog = null
                        },
                        onDismissRequest = { showNoteItemDialog = null },
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    if (displayNotes.isEmpty()) {
                        EmptyNote(modifier = Modifier.fillMaxSize())
                    } else {
                        LazyColumn(
                            modifier = Modifier,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp)
                        ) {
                            items(
                                items = displayNotes,
                                key = { it.docId.hashCode() }
                            ) { note ->
                                val isSelected = selectedNotes
                                    .find { it == note.docId } != null

                                NoteItem(
                                    note = note,
                                    selectedMode = isInSelectMode,
                                    isSelected = isSelected,
                                    onClick = {
                                        if (note.docId == null) return@NoteItem
                                        if (!isInSelectMode) {
                                            navController.navigate(
                                                Screen.NoteDetail.route.replace(
                                                    "{noteId}", note.docId
                                                )
                                            )
                                        } else {
                                            if (!isSelected) selectedNotes.add(note.docId)
                                            else selectedNotes.removeIf { it == note.docId }
                                        }
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
                                    onLongClick = { if (!isInSelectMode) showNoteItemDialog = note }
                                )
                            }
                        }
                    }
                }

                AnimatedContent(targetState = isInSelectMode) {
                    if (!it) {
                        AddNoteInput(
                            value = noteName,
                            onValueChange = { value -> noteName = value },
                            onAdd = {
                                if (noteName.isNotBlank()) {
                                    val name = noteName
                                    noteName = ""
                                    noteViewModel.addNote(
                                        Note(
                                            name = name, description = "", cover = null,
                                            completed = false, archived = false, isLoading = true,
                                            createdAt = Timestamp.now()
                                        )
                                    )
                                }
                            },
                            onCancel = { focusManager.clearFocus() },
                            isFocused = isFocused,
                            interactionSource = interactionSource,
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(
                                        topStartPercent = 25,
                                        topEndPercent = 25
                                    )
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            TextButton(
                                enabled = selectedNotes.isNotEmpty(),
                                onClick = {
                                    noteViewModel.archiveNotes(selectedNotes.toList(), true)
                                },
                            ) { Text(text = "Archive") }
                            Text(
                                text = "${selectedNotes.size} Selected",
                                fontSize = 16.sp, lineHeight = 16.sp,
                                letterSpacing = (0.2).sp,
                                fontWeight = FontWeight.Medium
                            )
                            TextButton(
                                enabled = selectedNotes.isNotEmpty(),
                                onClick = {
                                    dialogState = ConfirmDialogState(
                                        title = "Delete notes",
                                        message = "Are you sure to delete these notes?",
                                        confirmText = "Delete", cancelText = "Cancel",
                                        onConfirm = {
                                            dialogState = null
                                            noteViewModel.deleteNotes(selectedNotes.toList())
                                        },
                                        onCancel = { dialogState = null }
                                    )
                                }
                            ) { Text(text = "Delete") }
                        }
                    }
                }
            } else LoadingScreen()
        }
    }
}

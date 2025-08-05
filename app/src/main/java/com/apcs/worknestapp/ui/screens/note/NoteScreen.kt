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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.note.Note
import com.apcs.worknestapp.data.remote.note.NoteViewModel
import com.apcs.worknestapp.ui.components.LoadingScreen
import com.apcs.worknestapp.ui.components.bottombar.MainBottomBar
import com.apcs.worknestapp.ui.components.topbar.MainTopBar
import com.apcs.worknestapp.ui.screens.Screen
import com.apcs.worknestapp.ui.theme.Roboto
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

    var isFirstLoad by remember { mutableStateOf(true) }
    var isInSelectMode by remember { mutableStateOf(false) }
    var showActionMenu by remember { mutableStateOf(false) }
    var shouldShowNoteItemDialog by remember { mutableStateOf<Note?>(null) }

    val notes = noteViewModel.notes.collectAsState()
    val displayNotes = notes.value
        .filterNot { it.archived == true }
        .sortedByDescending { it.createdAt }
    var isRefreshing by remember { mutableStateOf(false) }

    var noteName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (notes.value.isEmpty()) {
            isRefreshing = true
            noteViewModel.refreshNotesIfEmpty()
            isRefreshing = false
        }
        isFirstLoad = false
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
                        onClick = { showActionMenu = true },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                            disabledContentColor = Color.Unspecified,
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.symbol_three_dot),
                            contentDescription = "More options",
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(-90f)
                        )
                        DropdownMenu(
                            expanded = showActionMenu,
                            onDismissRequest = { showActionMenu = false },
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        ) {
                            val dropdownTextStyle = TextStyle(
                                fontSize = 14.sp, lineHeight = 14.sp,
                                fontFamily = Roboto, fontWeight = FontWeight.Normal,
                            )
                            val horizontalPadding = 20.dp
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Change background",
                                        style = dropdownTextStyle
                                    )
                                },
                                onClick = {}, //  onEditClick() },
                                trailingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.outline_palette),
                                        contentDescription = "Change background",
                                        modifier = Modifier.size(24.dp),
                                    )
                                },
                                contentPadding = PaddingValues(horizontal = horizontalPadding)
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Delete all",
                                        style = dropdownTextStyle
                                    )
                                },
                                onClick = {
                                    coroutineScope.launch {
                                        val isSuccess = noteViewModel.deleteAllNote()
                                        if (!isSuccess) {
                                            snackbarHost.showSnackbar(
                                                message = "Delete all note failed",
                                                withDismissAction = true,
                                            )
                                        }
                                        showActionMenu = false
                                    }
                                },
                                trailingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.outline_trash),
                                        contentDescription = "Delete all",
                                        modifier = Modifier.size(24.dp),
                                    )
                                },
                                contentPadding = PaddingValues(horizontal = horizontalPadding)
                            )
                        }
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
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        coroutineScope.launch {
                            isRefreshing = true
                            val isSuccess = noteViewModel.refreshNotes()
                            isRefreshing = false

                            if (!isSuccess) {
                                snackbarHost.showSnackbar(
                                    message = "Refresh notes failed. Something not work",
                                    withDismissAction = true,
                                )
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    if (displayNotes.isEmpty()) {
                        EmptyNote(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
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
                                                val isSuccess = noteViewModel.updateNoteComplete(
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

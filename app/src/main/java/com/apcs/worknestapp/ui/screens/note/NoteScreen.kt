package com.apcs.worknestapp.ui.screens.note

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.apcs.worknestapp.ui.components.bottombar.MainBottomBar
import com.apcs.worknestapp.ui.components.topbar.MainTopBar
import com.apcs.worknestapp.ui.screens.Screen
import com.apcs.worknestapp.ui.theme.Roboto
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
    var isInSelectMode by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val notes = noteViewModel.notes.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    var noteName by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        if (notes.value.isEmpty()) {
            isRefreshing = true
            noteViewModel.refreshNotesIfEmpty()
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            MainTopBar(
                title = if (isInSelectMode) "Select notes" else Screen.Note.title,
                actions = {
                    if (notes.value.isNotEmpty()) {
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
                        onClick = { showMenu = true },
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
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            shadowElevation = 32.dp,
                            shape = RoundedCornerShape(25f),
                            modifier = Modifier.widthIn(min = 200.dp),
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Change background",
                                        fontSize = 14.sp,
                                        lineHeight = 14.sp,
                                        fontFamily = Roboto,
                                        fontWeight = FontWeight.Normal,
                                    )
                                },
                                onClick = {}, //  onEditClick() },
                                trailingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.outline_palette),
                                        contentDescription = "Change background",
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Delete all",
                                        fontSize = 14.sp,
                                        lineHeight = 14.sp,
                                        fontFamily = Roboto,
                                        fontWeight = FontWeight.Normal,
                                    )
                                },
                                onClick = {},// onDeleteAllClick,
                                trailingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.outline_trash),
                                        contentDescription = "Delete all",
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            )
                            // ... more items ...
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
                if (notes.value.isEmpty()) {
                    EmptyNote(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        items(
                            items = notes.value,
                            key = { note -> note.docId.hashCode() }
                        ) { note ->
                            NoteItem(
                                note = note,
                                onClick = {
                                    navController.navigate(
                                        Screen.NoteDetail.route.replace(
                                            "{noteId}",
                                            note.docId ?: ""
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }

            AddNoteInput(
                value = noteName,
                onValueChange = { noteName = it },
                onCancel = { focusManager.clearFocus() },
                onAdd = {
                    if (noteName.isNotBlank()) {
                        coroutineScope.launch {
                            noteViewModel.addNote(
                                Note(
                                    name = noteName,
                                )
                            )
                        }
                    }
                },
                isFocused = isFocused,
                interactionSource = interactionSource,
            )
        }
    }
}

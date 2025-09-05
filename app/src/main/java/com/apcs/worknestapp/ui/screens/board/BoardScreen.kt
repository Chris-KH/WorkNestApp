package com.apcs.worknestapp.ui.screens.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavHostController
import com.apcs.worknestapp.data.remote.board.BoardViewModel
import com.apcs.worknestapp.data.remote.board.NoteList
import com.apcs.worknestapp.data.remote.note.Note
import com.apcs.worknestapp.ui.components.LoadingScreen
import com.apcs.worknestapp.ui.components.inputfield.CustomTextField
import com.apcs.worknestapp.ui.components.topbar.TopBarDefault
import com.apcs.worknestapp.ui.theme.Roboto
import com.apcs.worknestapp.utils.ColorUtils
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardScreen(
    boardId: String,
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
    boardViewModel: BoardViewModel = hiltViewModel(),
) {
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    var isFirstLoad by rememberSaveable { mutableStateOf(true) }
    val topAppBarColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)

    val board = boardViewModel.currentBoard.collectAsState()
    val boardCoverColor = board.value?.cover?.let { ColorUtils.safeParse(it) }
    var editableBoardName by remember(board.value?.name) { mutableStateOf(board.value?.name ?: "") }

    LaunchedEffect(Unit) {
        isFirstLoad = true
        val remoteBoard = boardViewModel.getBoard(boardId)
        if (remoteBoard == null) {
            navController.popBackStack()
            snackbarHost.showSnackbar(
                message = "Load board failed. Board not founded",
                withDismissAction = true,
            )
        }
        isFirstLoad = false
    }

    LaunchedEffect(board.value) {
        if (board.value == null && !isFirstLoad) {
            delay(1000)
            navController.popBackStack()
        }
    }

    LifecycleResumeEffect(boardId) {
        boardViewModel.registerNoteListListener(boardId)
        onPauseOrDispose {
            boardViewModel.removeNoteListListener()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (!isFirstLoad && board.value != null) {
                        CustomTextField(
                            value = editableBoardName,
                            onValueChange = { editableBoardName = it },
                            textStyle = TextStyle(
                                fontWeight = FontWeight.Normal,
                                fontFamily = Roboto,
                                fontSize = 16.sp,
                                lineHeight = 16.sp,
                                letterSpacing = (0).sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            containerColor = Color.Transparent,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    coroutineScope.launch {
                                        val initialName = board.value?.name ?: ""
                                        if (editableBoardName.isNotBlank() && editableBoardName != initialName) {
                                            val message = boardViewModel.updateBoardName(
                                                boardId,
                                                editableBoardName
                                            )
                                            if (message != null) {
                                                focusManager.clearFocus()
                                                editableBoardName = initialName
                                                snackbarHost.showSnackbar(
                                                    message = message,
                                                    withDismissAction = true,
                                                )
                                            } else focusManager.clearFocus()
                                        } else focusManager.clearFocus()
                                    }
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 8.dp),
                        )
                    } else {
                        Text(
                            text = "Loading Board...",
                            fontSize = 16.sp,
                            lineHeight = 16.sp,
                            fontFamily = Roboto,
                            letterSpacing = (0.25).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Board modal",
                            modifier = Modifier
                                .size(28.dp)
                                .rotate(90f)
                        )
                    }
                },
                expandedHeight = TopBarDefault.expandedHeight,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = topAppBarColor,
                    scrolledContainerColor = topAppBarColor,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        },
        containerColor = if (!isFirstLoad && board.value != null) boardCoverColor ?: Color.Gray
        else MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (isFirstLoad || board.value == null) {
            LoadingScreen(modifier = Modifier.padding(innerPadding))
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(
                        items = board.value?.noteLists ?: emptyList(),
                        key = { it.docId ?: UUID.randomUUID() }
                    ) { noteList ->
                        NoteListCard(
                            boardId = boardId,
                            noteList = noteList,
                            boardViewModel = boardViewModel,
                            onAddNoteClick = { listId, newNoteName ->
                                coroutineScope.launch {
                                    val newNote = Note(
                                        name = newNoteName,
                                        createdAt = Timestamp.now()
                                    )
                                    boardViewModel.addNoteToList(boardId, listId, newNote)
                                }
                            },
                            onNoteClick = { note ->
                                if (note.docId != null) {
                                    navController.navigate(
                                        "board_note_detail/${boardId}/${noteList.docId}/${note.docId}"
                                    )
                                } else {
                                    coroutineScope.launch {
                                        snackbarHost.showSnackbar(
                                            message = "Cannot open note: Note ID is missing.",
                                            withDismissAction = true
                                        )
                                    }
                                }
                            },
                            onUpdateNoteListName = { newName ->
                                val noteListId = noteList.docId
                                if (noteListId != null) {
                                    coroutineScope.launch {
                                        boardViewModel.updateNoteListName(
                                            boardId,
                                            noteListId,
                                            newName
                                        )
                                    }
                                }
                            },
                            onNoteCheckedChange = { note, isChecked ->
                                boardViewModel.updateNoteCheckedStatus(
                                    boardId, noteList.docId!!, note.docId!!, isChecked
                                )
                            },
                            onRemoveNoteList = {
                                coroutineScope.launch {
                                    boardViewModel.removeNoteList(boardId, noteList.docId!!)
                                }
                            },
                            onRemoveSpecificNote = { listId, noteId ->
                                coroutineScope.launch {
                                    boardViewModel.removeNoteFromNoteList(boardId, listId, noteId)
                                }
                            },

                            modifier = Modifier
                                .width(300.dp)
                        )
                    }
                    item(key = "Add note list button") {
                        Button(
                            onClick = {
                                boardViewModel.addNoteList(boardId, NoteList())
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = topAppBarColor,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 0.dp, horizontal = 20.dp),
                            modifier = Modifier.width(300.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add Note List",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Add new list",
                                fontSize = 14.sp,
                                lineHeight = 14.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

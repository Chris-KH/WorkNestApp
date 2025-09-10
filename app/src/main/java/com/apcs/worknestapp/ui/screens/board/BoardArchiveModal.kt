package com.apcs.worknestapp.ui.screens.board

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.apcs.worknestapp.data.remote.board.Board
import com.apcs.worknestapp.data.remote.board.BoardViewModel
import com.apcs.worknestapp.ui.components.CustomSnackBar
import com.apcs.worknestapp.ui.screens.Screen
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardArchiveModal(
    board: Board,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    navController: NavHostController,
    boardViewModel: BoardViewModel,
) {
    val modalSnackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = {
            it != SheetValue.Hidden
        }
    )
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val titles = listOf("Notes", "Lists")
    val archivedNoteLists = board.noteLists.filter { it.archived == true }
    val listHasArchivedNote = board.noteLists.filter { noteList ->
        noteList.archived == true || noteList.notes.any { it.archived == true }
    }

    // To avoid click spam to unarchive noteList
    val noteListClicked = remember { mutableStateMapOf<String, Boolean>() }

    ModalBottomSheet(
        sheetState = sheetState,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        onDismissRequest = {
            coroutineScope.launch {
                sheetState.hide()
                onDismissRequest()
            }
        },
        modifier = modifier
            .fillMaxSize()
            .padding(WindowInsets.statusBars.asPaddingValues()),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SnackbarHost(
                hostState = modalSnackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(1000f)
            ) { CustomSnackBar(data = it) }

            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                onDismissRequest()
                            }
                        },
                        modifier = Modifier.align(alignment = Alignment.CenterStart)
                    ) { Icon(Icons.Default.Close, contentDescription = null) }
                    Text(
                        text = "Archive",
                        modifier = Modifier.align(alignment = Alignment.Center)
                    )
                }

                TabRow(selectedTabIndex = selectedTabIndex) {
                    titles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) },
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                AnimatedContent(
                    targetState = selectedTabIndex,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { tabIndex ->
                    if (tabIndex == 0) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 12.dp)
                        ) {
                            listHasArchivedNote.forEach { noteList ->
                                val archivedNotes = if (noteList.archived == true) noteList.notes
                                else noteList.notes.filter { it.archived == true }

                                items(
                                    items = archivedNotes,
                                    key = { note -> note.docId ?: UUID.randomUUID() }
                                ) { note ->
                                    BoardNoteItem(
                                        note = note,
                                        board = board,
                                        onClick = {
                                            val boardId = board.docId ?: ""
                                            val noteListId = noteList.docId ?: ""
                                            val noteId = note.docId ?: ""
                                            navController.navigate(
                                                Screen.BoardNoteDetail.route + "/$boardId/$noteListId/$noteId"
                                            ) {
                                                restoreState = true
                                            }
                                        },
                                        onCheckedChange = {
                                            val boardId = board.docId
                                            val noteListId = noteList.docId
                                            val noteId = note.docId
                                            if (boardId != null && noteListId != null && noteId != null) {
                                                coroutineScope.launch {
                                                    val currentState = note.completed
                                                    val message =
                                                        boardViewModel.updateNoteComplete(
                                                            boardId,
                                                            noteListId,
                                                            noteId,
                                                            currentState != true,
                                                        )
                                                    if (message != null) {
                                                        modalSnackbarHostState.showSnackbar(
                                                            message = message,
                                                            withDismissAction = true,
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier.animateItem()
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "In list: ${noteList.name ?: ""}",
                                        fontSize = 13.sp,
                                        lineHeight = 16.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    } else if (tabIndex == 1) {
                        if (archivedNoteLists.isEmpty()) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No archived lists",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                items(
                                    items = archivedNoteLists,
                                    key = { it.docId ?: UUID.randomUUID() },
                                ) { noteList ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .animateItem()
                                            .clickable(
                                                onClick = {
                                                    val boardId = board.docId
                                                    val noteListId = noteList.docId
                                                    if (boardId != null && noteListId != null)
                                                        coroutineScope.launch {
                                                            noteListClicked[noteListId] = true
                                                            val message =
                                                                boardViewModel.updateNoteListArchive(
                                                                    boardId,
                                                                    noteListId,
                                                                    false,
                                                                )
                                                            if (message != null) {
                                                                noteListClicked[noteListId] = false
                                                                modalSnackbarHostState.showSnackbar(
                                                                    message = message,
                                                                    withDismissAction = true,
                                                                )
                                                            } else {
                                                                noteListClicked.remove(noteListId)
                                                            }
                                                        }
                                                },
                                                enabled = noteList.docId != null && noteListClicked[noteList.docId] != true
                                            )
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp, horizontal = 20.dp)
                                    ) {
                                        Text(
                                            text = noteList.name ?: "",
                                            fontSize = 16.sp,
                                            lineHeight = 16.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Unarchive",
                                            fontSize = 16.sp,
                                            lineHeight = 16.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

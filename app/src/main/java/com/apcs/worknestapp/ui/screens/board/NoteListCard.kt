package com.apcs.worknestapp.ui.screens.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.apcs.worknestapp.data.remote.board.Board
import com.apcs.worknestapp.data.remote.board.BoardViewModel
import com.apcs.worknestapp.data.remote.board.NoteList
import com.apcs.worknestapp.data.remote.note.Note
import com.apcs.worknestapp.ui.components.inputfield.CustomTextField
import com.apcs.worknestapp.ui.screens.Screen
import com.apcs.worknestapp.ui.theme.Roboto
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun NoteListCard(
    boardId: String,
    noteList: NoteList,
    board: Board,
    onUpdateNoteListName: (String) -> Unit,
    onRemoveNoteList: () -> Unit,
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
    boardViewModel: BoardViewModel,
) {
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    var newNoteName by rememberSaveable(noteList.docId) { mutableStateOf("") }
    val notes = noteList.notes

    var editableNoteListName by remember(noteList.name, noteList.docId) {
        mutableStateOf(noteList.name ?: "")
    }

    fun addNewNote(boardId: String, noteListId: String, note: Note) {
        coroutineScope.launch {
            val message = boardViewModel.addNoteToList(boardId, noteListId, note)
            if (message != null) {
                snackbarHost.showSnackbar(
                    message = message,
                    withDismissAction = true,
                )
            }
        }
    }

    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 10.dp)
                .fillMaxWidth()
        ) {
            var showDropdown by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CustomTextField(
                    value = editableNoteListName,
                    onValueChange = { editableNoteListName = it },
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontFamily = Roboto,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        letterSpacing = (0.25).sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    containerColor = Color.Transparent,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (noteList.docId != null &&
                                editableNoteListName.isNotBlank() &&
                                editableNoteListName != (noteList.name ?: "")
                            ) {
                                onUpdateNoteListName(editableNoteListName)
                            } else if (noteList.docId != null && editableNoteListName.isBlank() && noteList.name?.isNotBlank() == true) {
                                editableNoteListName = noteList.name
                            }
                            focusManager.clearFocus()
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 40.dp)
                        .padding(end = 8.dp)
                        .padding(vertical = 8.dp)
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused) {
                                val initialName = noteList.name ?: ""
                                if (editableNoteListName.isNotBlank() && editableNoteListName != initialName) {
                                    onUpdateNoteListName(editableNoteListName)
                                } else {
                                    editableNoteListName = initialName
                                }
                            }
                        },
                )
                IconButton(
                    onClick = { showDropdown = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Note list options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.rotate(90f)
                    )
                    NoteListDropdown(
                        expanded = showDropdown,
                        isNoteEmpty = notes.isEmpty(),
                        onDismissRequest = { showDropdown = false },
                        onArchiveCompletedNotes = {
                            showDropdown = false
                        },
                        onArchiveAllNotes = {
                            showDropdown = false
                        },
                        onDeleteAllNotes = {
                            showDropdown = false
                        },
                        onDeleteNoteList = {
                            showDropdown = false
                            onRemoveNoteList()
                        },
                    )
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(
                    items = notes,
                    key = { note -> note.docId ?: UUID.randomUUID() }
                ) { note ->
                    BoardNoteItem(
                        note = note,
                        board = board,
                        onClick = {
                            val noteListId = noteList.docId ?: ""
                            val noteId = note.docId ?: ""
                            navController.navigate(
                                Screen.BoardNoteDetail.route + "/$boardId/$noteListId/$noteId"
                            ) {
                                restoreState = true
                            }
                        },
                        onCheckedChange = { isChecked ->

                        },
                        onRemoveThisNote = {

                        }
                    )
                }
            }

            TextField(
                value = newNoteName,
                onValueChange = { newNoteName = it },
                placeholder = { Text("+ Add note") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        val noteListId = noteList.docId
                        if (newNoteName.isNotBlank() && noteListId != null) {
                            addNewNote(boardId, noteList.docId, Note(name = newNoteName))
                            newNoteName = ""
                            focusManager.clearFocus()
                        }
                    }
                ),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            val noteListId = noteList.docId
                            if (newNoteName.isNotBlank() && noteListId != null) {
                                addNewNote(boardId, noteList.docId, Note(name = newNoteName))
                                newNoteName = ""
                            }
                        },
                        enabled = newNoteName.isNotBlank()
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Note")
                    }
                },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
            )
        }
    }
}

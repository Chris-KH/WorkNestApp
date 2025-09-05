package com.apcs.worknestapp.ui.components.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.apcs.worknestapp.data.remote.board.BoardViewModel
import com.apcs.worknestapp.data.remote.board.NoteList
import com.apcs.worknestapp.data.remote.note.Note

@Composable
fun NoteListCard(
    noteList: NoteList,
    boardId: String,
    onAddNoteClick: (listId: String, noteName: String) -> Unit,
    onNoteClick: (Note) -> Unit,
    onNoteCheckedChange: (Note, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onRemoveNoteList: () -> Unit,
    onRemoveSpecificNote: (listId: String, noteId: String) -> Unit,
    onUpdateNoteListName: (boardId: String, noteListId: String, newName: String) -> Unit,
    boardViewModel: BoardViewModel,
) {
    val focusManager = LocalFocusManager.current
    var newNoteName by rememberSaveable(noteList.docId) { mutableStateOf("") }

    var editableNoteListName by remember(noteList.name, noteList.docId) {
        mutableStateOf(noteList.name.takeIf { it?.isNotBlank() == true } ?: "")
    }
    LaunchedEffect(noteList.name) {
        if (noteList.name != editableNoteListName) {
            editableNoteListName = noteList.name.takeIf { it?.isNotBlank() == true } ?: ""
        }
        boardViewModel.getNotesForNoteList(boardId, noteList.docId!!)

    }
    var isEditingListName by remember { mutableStateOf(false) }
    Card(
        modifier = modifier.fillMaxHeight(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 12.dp)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BasicTextField(
                    value = editableNoteListName,
                    onValueChange = { editableNoteListName = it },
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (noteList.docId != null &&
                                editableNoteListName.isNotBlank() &&
                                editableNoteListName != (noteList.name ?: "")
                            ) {
                                onUpdateNoteListName(boardId, noteList.docId, editableNoteListName)
                            } else if (noteList.docId != null && editableNoteListName.isBlank() && noteList.name?.isNotBlank() == true) {
                                editableNoteListName = noteList.name!!
                            }
                            isEditingListName = false
                            focusManager.clearFocus()
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .onFocusChanged { focusState ->
                            isEditingListName = focusState.isFocused
                            if (!focusState.isFocused) { // Focus lost
                                if (noteList.docId != null &&
                                    editableNoteListName.isNotBlank() &&
                                    editableNoteListName != (noteList.name ?: "")
                                ) {
                                    onUpdateNoteListName(
                                        boardId,
                                        noteList.docId,
                                        editableNoteListName
                                    )
                                } else if (noteList.docId != null && editableNoteListName.isBlank() && noteList.name?.isNotBlank() == true) {
                                    editableNoteListName = noteList.name!!
                                }
                            }
                        }
                        .onKeyEvent { keyEvent ->
                            if (keyEvent.key == Key.Enter) {
                                if (noteList.docId != null &&
                                    editableNoteListName.isNotBlank() &&
                                    editableNoteListName != (noteList.name ?: "")
                                ) {
                                    onUpdateNoteListName(
                                        boardId,
                                        noteList.docId,
                                        editableNoteListName
                                    )
                                } else if (noteList.docId != null && editableNoteListName.isBlank() && noteList.name?.isNotBlank() == true) {
                                    editableNoteListName = noteList.name!!
                                }
                                isEditingListName = false
                                focusManager.clearFocus()
                                true // Consume the event
                            } else {
                                false
                            }
                        },
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField -> // To make it look like regular text when not focused
                        if (isEditingListName) {
                            innerTextField()
                        } else {
                            Text(
                                text = editableNoteListName.takeIf { it.isNotBlank() }
                                    ?: "Unnamed List",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                )
                IconButton(onClick = onRemoveNoteList) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Remove List"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.weight(1f)) {
                val notes by boardViewModel.notes.collectAsState()

                if (notes.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No notes in this list yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(
                            items = notes,
                            key = { note -> note.docId ?: note.name.hashCode() }
                        ) { note ->
                            NoteListItem(
                                note = note,
                                onClick = { onNoteClick(note) },
                                onCheckedChange = { isChecked ->
                                    onNoteCheckedChange(note, isChecked)
                                },
                                onRemoveThisNote = {
                                    if (noteList.docId != null && note.docId != null) {
                                        onRemoveSpecificNote(noteList.docId, note.docId)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = newNoteName,
                onValueChange = { newNoteName = it },
                label = { Text("Add a note") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (newNoteName.isNotBlank() && noteList.docId != null) {
                            onAddNoteClick(noteList.docId, newNoteName)
                            newNoteName = ""
                            focusManager.clearFocus()
                        }
                    }
                ),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (newNoteName.isNotBlank() && noteList.docId != null) {
                                onAddNoteClick(noteList.docId, newNoteName)
                                newNoteName = ""
                            }
                        },
                        enabled = newNoteName.isNotBlank()
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Note")
                    }
                }
            )
        }
    }
}

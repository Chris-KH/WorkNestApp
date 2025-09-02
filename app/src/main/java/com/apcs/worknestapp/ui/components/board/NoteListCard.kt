package com.apcs.worknestapp.ui.components.board

import androidx.compose.animation.core.copy
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.apcs.worknestapp.data.remote.board.Notelist
import com.apcs.worknestapp.data.remote.note.Note


@Composable
fun NoteListCard(
    notelist: Notelist,
    onAddNoteClick: (listId: String, noteName: String) -> Unit,
    onNoteClick: (Note) -> Unit,
    onNoteCheckedChange: (Note, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onRemoveNotelist: () -> Unit,
    onRemoveSpecificNote: (listId: String, noteId: String) -> Unit,
    onUpdateNotelistName: (boardId: String, notelistId: String, newName: String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var newNoteName by rememberSaveable(notelist.docId) { mutableStateOf("") }

    var editableNotelistName by remember(notelist.name, notelist.docId) {
        mutableStateOf(notelist.name.takeIf { it?.isNotBlank() == true } ?: "")
    }
    LaunchedEffect(notelist.name) {
        if (notelist.name != editableNotelistName) {
            editableNotelistName = notelist.name.takeIf { it?.isNotBlank() == true } ?: ""
        }
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
                    value = editableNotelistName,
                    onValueChange = { editableNotelistName = it },
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (notelist.docId != null &&
                                editableNotelistName.isNotBlank() &&
                                editableNotelistName != (notelist.name ?: "")
                            ) {
                                onUpdateNotelistName(notelist.boardId.toString(), notelist.docId, editableNotelistName)
                            } else if (notelist.docId != null && editableNotelistName.isBlank() && notelist.name?.isNotBlank() == true) {
                                editableNotelistName = notelist.name!!
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
                                if (notelist.docId != null &&
                                    editableNotelistName.isNotBlank() &&
                                    editableNotelistName != (notelist.name ?: "")
                                ) {
                                    onUpdateNotelistName(notelist.boardId.toString(), notelist.docId, editableNotelistName)
                                } else if (notelist.docId != null && editableNotelistName.isBlank() && notelist.name?.isNotBlank() == true) {
                                    editableNotelistName = notelist.name!!
                                }
                            }
                        }
                        .onKeyEvent { keyEvent ->
                            if (keyEvent.key == Key.Enter) {
                                if (notelist.docId != null &&
                                    editableNotelistName.isNotBlank() &&
                                    editableNotelistName != (notelist.name ?: "")
                                ) {
                                    onUpdateNotelistName(notelist.boardId.toString(), notelist.docId, editableNotelistName)
                                } else if (notelist.docId != null && editableNotelistName.isBlank() && notelist.name?.isNotBlank() == true) {
                                    editableNotelistName = notelist.name!!
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
                                text = editableNotelistName.takeIf { it.isNotBlank() } ?: "Unnamed List",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                )
                IconButton(onClick = onRemoveNotelist) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Remove List"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (notelist.notes.isEmpty()) {
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
                            items = notelist.notes,
                            key = { note -> note.docId ?: note.name.hashCode() }
                        ) { note ->
                            NoteListItem(
                                note = note,
                                onClick = { onNoteClick(note) },
                                onCheckedChange = { isChecked ->
                                    onNoteCheckedChange(note, isChecked)
                                },
                                onRemoveThisNote = {
                                    if (notelist.docId != null && note.docId != null) {
                                        onRemoveSpecificNote(notelist.docId, note.docId)
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
                        if (newNoteName.isNotBlank() && notelist.docId != null) {
                            onAddNoteClick(notelist.docId, newNoteName)
                            newNoteName = ""
                            focusManager.clearFocus()
                        }
                    }
                ),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (newNoteName.isNotBlank() && notelist.docId != null) {
                                onAddNoteClick(notelist.docId, newNoteName)
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


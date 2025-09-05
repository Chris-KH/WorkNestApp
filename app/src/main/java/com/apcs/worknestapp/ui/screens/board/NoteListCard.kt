package com.apcs.worknestapp.ui.screens.board

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.board.BoardViewModel
import com.apcs.worknestapp.data.remote.board.NoteList
import com.apcs.worknestapp.data.remote.note.Note
import com.apcs.worknestapp.ui.components.inputfield.CustomTextField
import com.apcs.worknestapp.ui.theme.Roboto

@Composable
fun NoteListCard(
    boardId: String,
    noteList: NoteList,
    onAddNoteClick: (listId: String, noteName: String) -> Unit,
    onNoteClick: (Note) -> Unit,
    onUpdateNoteListName: (String) -> Unit,
    onRemoveNoteList: () -> Unit,
    onNoteCheckedChange: (Note, Boolean) -> Unit,
    onRemoveSpecificNote: (listId: String, noteId: String) -> Unit,
    modifier: Modifier = Modifier,
    boardViewModel: BoardViewModel,
) {
    val focusManager = LocalFocusManager.current
    var newNoteName by rememberSaveable(noteList.docId) { mutableStateOf("") }
    val notes = noteList.notes

    var editableNoteListName by remember(noteList.name, noteList.docId) {
        mutableStateOf(noteList.name ?: "")
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center,
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
                CustomTextField(
                    value = editableNoteListName,
                    onValueChange = { editableNoteListName = it },
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontFamily = Roboto,
                        fontSize = 14.sp,
                        lineHeight = 14.sp,
                        letterSpacing = (0.1).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
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
                        .padding(end = 8.dp)
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused) { // Focus lost
                                if (noteList.docId != null &&
                                    editableNoteListName.isNotBlank() &&
                                    editableNoteListName != (noteList.name ?: "")
                                ) {
                                    onUpdateNoteListName(editableNoteListName)
                                } else if (noteList.docId != null && editableNoteListName.isBlank() && noteList.name?.isNotBlank() == true) {
                                    editableNoteListName = noteList.name
                                }
                            }
                        },
                )
                IconButton(onClick = onRemoveNoteList) {
                    Icon(
                        painter = painterResource(R.drawable.fill_trash),
                        contentDescription = "Remove List",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.weight(1f)) {
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
                            NoteItem(
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

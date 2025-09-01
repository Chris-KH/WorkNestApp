package com.apcs.worknestapp.ui.components.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apcs.worknestapp.data.remote.board.Notelist
import com.apcs.worknestapp.data.remote.note.Note
import kotlinx.coroutines.Job

@Composable
fun NoteListCard(
    notelist: Notelist,
    onAddNoteClick: (listId: String, noteName: String) -> Unit,
    onNoteClick: (Note) -> Unit,
    onNoteCheckedChange: (Note, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onRemoveNotelist: () -> Unit,
    onRemoveSpecificNote: (listId: String, noteId: String) -> Unit
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = notelist.name ?: "Unnamed List",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onRemoveNotelist) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Remove List"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                notelist.notes.forEach { note ->
                    NoteListItem(
                        note = note,
                        onClick = { onNoteClick(note) },
                        onCheckedChange = { isChecked -> onNoteCheckedChange(note, isChecked) },
                        onRemoveThisNote = { onRemoveSpecificNote(notelist.docId!!, note.docId!!) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            var newNoteName by rememberSaveable(notelist.docId) { mutableStateOf("") }
            OutlinedTextField(
                value = newNoteName,
                onValueChange = { newNoteName = it },
                label = { Text("New note name")},
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = {
                        if (newNoteName.isNotBlank() && notelist.docId != null) {
                            onAddNoteClick(notelist.docId, newNoteName)
                            newNoteName = ""
                        }
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Note")
                    }
                }
            )
        }
    }
}
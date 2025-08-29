package com.apcs.worknestapp.ui.components.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apcs.worknestapp.data.remote.board.Notelist
import com.apcs.worknestapp.data.remote.note.Note

@Composable
fun NoteListCard(
    notelist: Notelist,
    onAddNoteClick: (listId: String, noteName: String) -> Unit,
    onNoteClick: (Note) -> Unit,
    onNoteCheckedChange: (Note, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxHeight(), // Make card take full height of the LazyRow
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = notelist.name ?: "Unnamed List",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Column for notes within this list (vertically scrollable if needed)
            // For simplicity, this example doesn't make this inner list scrollable,
            // but for many notes, you'd wrap this in a LazyColumn or Column with verticalScroll.
            Column(
                modifier = Modifier.weight(1f), // Takes available space before the add button
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                notelist.notes.forEach { note ->
                    NoteListItem(
                        note = note,
                        onClick = { onNoteClick(note) },
                        onCheckedChange = { isChecked -> onNoteCheckedChange(note, isChecked) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Button to add a new note to this specific list
            var newNoteName by rememberSaveable(notelist.docId) { mutableStateOf("") } // Keyed by listId for unique state
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
                            newNoteName = "" // Clear after adding
                        }
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Note")
                    }
                }
            )
        }
    }
}
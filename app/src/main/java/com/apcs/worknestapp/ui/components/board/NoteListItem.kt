package com.apcs.worknestapp.ui.components.board

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apcs.worknestapp.data.remote.note.Note

@Composable
fun NoteListItem(
    note: Note,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onRemoveThisNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = note.completed ?: false,
                onCheckedChange = onCheckedChange
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (note.name?.isNotBlank() == true) note.name else "Untitled Note",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            if (note.isLoading == true) {
                Spacer(Modifier.width(4.dp))
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
            }
            IconButton(onClick = onRemoveThisNote, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Remove Note",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
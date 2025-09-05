package com.apcs.worknestapp.ui.components.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apcs.worknestapp.data.remote.board.ChecklistBoard


@Composable
fun ChecklistItemBoard(
    checklistBoard: ChecklistBoard,
    modifier: Modifier = Modifier,
    onChangeChecklistName: (String) -> Unit,
    onDeleteChecklist: () -> Unit,
) {
    var checklistName by remember(checklistBoard.name) {
        mutableStateOf(checklistBoard.name ?: "")
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedTextField(
            value = checklistName,
            onValueChange = {
                checklistName = it
                onChangeChecklistName(it)
            },
            label = { Text("Checklist Name") },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(onClick = onDeleteChecklist) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Checklist",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
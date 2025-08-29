package com.apcs.worknestapp.ui.components.board

import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun BoardActionDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onRenameBoard: () -> Unit,
    onChangeCover: () -> Unit,
    onManageMembers: () -> Unit,
    onDeleteBoard: () -> Unit
    // Add other actions as needed
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        DropdownMenuItem(
            text = { Text("Rename Board") },
            onClick = {
                onRenameBoard()
                onDismissRequest()
            }
        )
        DropdownMenuItem(
            text = { Text("Change Cover") },
            onClick = {
                onChangeCover()
                onDismissRequest()
            }
        )
        DropdownMenuItem(
            text = { Text("Manage Members") },
            onClick = {
                onManageMembers()
                onDismissRequest()
            }
        )
        DropdownMenuItem(
            text = { Text("Delete Board", color = MaterialTheme.colorScheme.error) },
            onClick = {
                onDeleteBoard()
                onDismissRequest()
            }
        )
    }
}
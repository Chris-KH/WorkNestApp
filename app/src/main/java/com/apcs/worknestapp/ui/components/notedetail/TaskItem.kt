package com.apcs.worknestapp.ui.components.notedetail

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.UUID


data class Task(
    val id: String = UUID.randomUUID().toString(),
    var text: String,
    var isCompleted: Boolean
    //TODO: Mock class
)

@Composable
fun TaskItemUI(
    task: Task,
    onTaskChange: (Task) -> Unit,
    onDeleteTask: () -> Unit
) {
    var internalTaskText by remember(task.text) { mutableStateOf(task.text) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = { onTaskChange(task.copy(isCompleted = it)) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedTextField(
            value = internalTaskText,
            onValueChange = { internalTaskText = it },
            modifier = Modifier.weight(1f),
            label = {Text("Task")},
            singleLine = true, // Or remove if tasks can be multi-line
            trailingIcon = {
                IconButton(onClick = {
                    onTaskChange(task.copy(text = internalTaskText))
                }, enabled = internalTaskText != task.text ) { // Enable only if changed
                    Icon(Icons.Filled.Check, contentDescription = "Save task text")
                }
            }
        )
        IconButton(onClick = onDeleteTask) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete task")
        }
    }
}
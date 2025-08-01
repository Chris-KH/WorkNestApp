package com.apcs.worknestapp.ui.components.notedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.UUID


data class WorklistItem(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var tasks: List<Task>,
    var isCollapsed: Boolean = false // For collapse/expand functionality
    //TODO: Mock class
)

@Composable
fun WorklistItemUI(
    worklistItem: WorklistItem,
    onWorklistItemChange: (WorklistItem) -> Unit,
    onDeleteWorklistItem: () -> Unit // Callback to delete this worklist item
) {
    var internalWorklistItemName by remember(worklistItem.name) { mutableStateOf(worklistItem.name) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = internalWorklistItemName,
                onValueChange = { internalWorklistItemName = it },
                label = { Text("List Name") },
                modifier = Modifier.weight(1f),
                trailingIcon = { // Save button for list name
                    IconButton(onClick = {
                        onWorklistItemChange(worklistItem.copy(name = internalWorklistItemName))
                    }, enabled = internalWorklistItemName != worklistItem.name) {
                        Icon(Icons.Filled.Check, contentDescription = "Save list name")
                    }
                }
            )
            IconButton(onClick = {
                onWorklistItemChange(worklistItem.copy(isCollapsed = !worklistItem.isCollapsed))
            }) {
                Icon(
                    if (worklistItem.isCollapsed) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                    contentDescription = if (worklistItem.isCollapsed) "Expand" else "Collapse"
                )
            }
            IconButton(onClick = onDeleteWorklistItem) { // Delete button for the whole list
                Icon(Icons.Filled.Delete, contentDescription = "Delete this task list")
            }
        }

        if (!worklistItem.isCollapsed) {
            Spacer(modifier = Modifier.height(8.dp))
            worklistItem.tasks.forEachIndexed { taskIndex, task ->
                TaskItemUI(
                    task = task,
                    onTaskChange = { updatedTask ->
                        val newTasks = worklistItem.tasks.toMutableList()
                        newTasks[taskIndex] = updatedTask
                        onWorklistItemChange(worklistItem.copy(tasks = newTasks))
                    },
                    onDeleteTask = {
                        val newTasks = worklistItem.tasks.filterNot { it.id == task.id }
                        onWorklistItemChange(worklistItem.copy(tasks = newTasks))
                    }
                )
            }
            TextButton(
                onClick = {
                    val newTask = Task(text = "New Task", isCompleted = false)
                    onWorklistItemChange(worklistItem.copy(tasks = worklistItem.tasks + newTask))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add task")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Task")
            }
        }
    }
}
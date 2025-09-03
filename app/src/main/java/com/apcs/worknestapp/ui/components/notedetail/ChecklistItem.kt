package com.apcs.worknestapp.ui.components.notedetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.note.Checklist
import com.apcs.worknestapp.data.remote.note.NoteViewModel
import com.apcs.worknestapp.ui.components.ConfirmDialog
import com.apcs.worknestapp.ui.components.ConfirmDialogState
import com.apcs.worknestapp.ui.components.inputfield.CustomTextField
import com.apcs.worknestapp.ui.theme.Roboto
import java.util.UUID

@Composable
fun ChecklistItem(
    noteId: String,
    checklist: Checklist,
    modifier: Modifier = Modifier,
    onAddNewTask: () -> Unit,
    onChangeChecklistName: (String) -> String,
    onDeleteChecklist: () -> Unit,
    noteViewModel: NoteViewModel,
) {
    var checklistName by remember(checklist.name) { mutableStateOf(checklist.name ?: "") }
    val tasks = checklist.tasks
    var isCollapsed by remember { mutableStateOf(false) }
    var hideCompleted by remember { mutableStateOf(false) }
    var showConfirmDeleteDialog by remember { mutableStateOf<ConfirmDialogState?>(null) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        showConfirmDeleteDialog?.let {
            ConfirmDialog(
                title = it.title, message = it.message,
                onDismissRequest = { showConfirmDeleteDialog = null },
                confirmText = it.confirmText, cancelText = it.cancelText,
                onConfirm = it.onConfirm, onCancel = it.onConfirm,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var showDropdownMenu by remember { mutableStateOf(false) }
            val rotation by animateFloatAsState(
                targetValue = if (isCollapsed) 0f else 180f, label = "arrowRotation"
            )
            val textStyle = TextStyle(
                fontSize = 15.sp, lineHeight = 18.sp, letterSpacing = (0.4).sp,
                fontFamily = Roboto, fontWeight = FontWeight.Medium
            )
            CustomTextField(
                value = checklistName,
                onValueChange = { checklistName = it },
                placeholder = {
                    Text(
                        text = "Checklist name",
                        style = textStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                textStyle = TextStyle(
                    fontSize = textStyle.fontSize,
                    lineHeight = textStyle.lineHeight,
                    fontFamily = textStyle.fontFamily,
                    letterSpacing = textStyle.letterSpacing,
                    fontWeight = textStyle.fontWeight,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                containerColor = Color.Transparent,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 20.dp)
                    .onFocusChanged { state ->
                        val initialName = checklist.name ?: ""
                        if (!state.isFocused && checklistName != initialName) {
                            if (checklistName.isBlank()) checklistName = initialName
                            else {
                                val newName = onChangeChecklistName(checklistName)
                                checklistName = newName
                            }
                        }
                    }
            )
            IconButton(onClick = { isCollapsed = !isCollapsed }, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = if (isCollapsed) "Expand" else "Collapse",
                    modifier = Modifier.rotate(rotation)
                )
            }
            IconButton(onClick = { showDropdownMenu = true }, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    modifier = Modifier.rotate(90f)
                )
                DropdownMenu(
                    expanded = showDropdownMenu,
                    onDismissRequest = { showDropdownMenu = false },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.widthIn(min = 240.dp)
                ) {
                    val dropdownTextStyle = TextStyle(
                        fontSize = 15.sp, lineHeight = 16.sp, letterSpacing = 0.sp,
                        fontFamily = Roboto, fontWeight = FontWeight.Normal,
                    )
                    val horizontalPadding = 20.dp
                    val iconSize = 24.dp

                    DropdownMenuItem(
                        text = { Text(text = "New task", style = dropdownTextStyle) },
                        onClick = {
                            showDropdownMenu = false
                            onAddNewTask()
                        },
                        trailingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.outline_add_task),
                                contentDescription = "Add new task",
                                modifier = Modifier.size(iconSize),
                            )
                        },
                        contentPadding = PaddingValues(horizontal = horizontalPadding)
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (hideCompleted) "Show completed"
                                else "Hide completed",
                                style = dropdownTextStyle
                            )
                        },
                        onClick = {
                            showDropdownMenu = false
                            hideCompleted = !hideCompleted
                        },
                        trailingIcon = {
                            Icon(
                                painter = painterResource(
                                    if (hideCompleted) R.drawable.outline_unseen
                                    else R.drawable.outline_seen
                                ),
                                contentDescription = if (hideCompleted) "Show completed"
                                else "Hide completed",
                                modifier = Modifier.size(iconSize),
                            )
                        },
                        contentPadding = PaddingValues(horizontal = horizontalPadding)
                    )
                    HorizontalDivider(
                        thickness = 8.dp,
                        color = MaterialTheme.colorScheme.surfaceContainer
                    )
                    DropdownMenuItem(
                        text = { Text(text = "Delete", style = dropdownTextStyle) },
                        onClick = {
                            showDropdownMenu = false
                            onDeleteChecklist()
                            /*showConfirmDeleteDialog = ConfirmDialogState(
                                title = "Delete checklist",
                                message = "Are you sure?",
                                confirmText = "Delete",
                                cancelText = "Cancel",
                                onConfirm = { onDeleteChecklist() },
                                onCancel = { showConfirmDeleteDialog = null }
                            )*/
                        },
                        trailingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.outline_trash),
                                contentDescription = "Delete note",
                                modifier = Modifier.size(iconSize),
                            )
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = MaterialTheme.colorScheme.error,
                            trailingIconColor = MaterialTheme.colorScheme.error,
                        ),
                        contentPadding = PaddingValues(horizontal = horizontalPadding)
                    )
                }
            }
        }
        if (tasks.isNotEmpty()) {
            val total = checklist.tasks.size
            val done = checklist.tasks.count { it.done == true }
            val progress = done.toFloat() / total.toFloat()

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    HorizontalDivider()
    AnimatedVisibility(
        visible = !isCollapsed,
        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            tasks.forEachIndexed { idx, task ->
                key(task.docId ?: UUID.randomUUID()) {
                    TaskItem(
                        task = task,
                        onToggleTask = {
                            val currentState = task.done == true
                            val checklistId = checklist.docId ?: return@TaskItem
                            val taskId = task.docId ?: return@TaskItem
                            noteViewModel.updateTaskDone(noteId, checklistId, taskId, !currentState)
                        },
                        onChangeTaskName = { newName ->
                            val initialName = task.name ?: ""
                            val checklistId = checklist.docId ?: return@TaskItem initialName
                            val taskId = task.docId ?: return@TaskItem initialName
                            val isSuccess = noteViewModel
                                .updateTaskName(noteId, checklistId, taskId, newName)
                            if (!isSuccess) {
                                return@TaskItem initialName
                            }

                            return@TaskItem newName
                        },
                        onDeleteTask = {
                            val checklistId = checklist.docId ?: return@TaskItem
                            val taskId = task.docId ?: return@TaskItem
                            noteViewModel.deleteTask(noteId, checklistId, taskId)
                        },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

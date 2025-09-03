package com.apcs.worknestapp.ui.components.notedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.note.Task
import com.apcs.worknestapp.ui.components.inputfield.CustomTextField
import com.apcs.worknestapp.ui.theme.Roboto
import com.apcs.worknestapp.ui.theme.onSuccess
import com.apcs.worknestapp.ui.theme.success

@Composable
fun TaskItem(
    task: Task,
    onChangeTaskName: (String) -> String,
    onToggleTask: () -> Unit,
    onDeleteTask: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var taskName by remember(task.name) { mutableStateOf(task.name ?: "") }
    val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) onDeleteTask()
            it != SwipeToDismissBoxValue.EndToStart
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.75f }
    )

    SwipeToDismissBox(
        state = swipeToDismissBoxState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(swipeToDismissBoxState.progress)
                        .background(color = MaterialTheme.colorScheme.error),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.fill_trash),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val textStyle = TextStyle(
                fontSize = 15.sp, lineHeight = 18.sp, letterSpacing = (0.4).sp,
                fontFamily = Roboto, fontWeight = FontWeight.Normal
            )

            Checkbox(
                checked = task.done == true,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.success,
                    checkmarkColor = MaterialTheme.colorScheme.onSuccess,
                ),
                onCheckedChange = { onToggleTask() }
            )
            CustomTextField(
                value = taskName,
                onValueChange = { taskName = it },
                placeholder = {
                    Text(
                        text = "Task name",
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
                    .onFocusChanged { state ->
                        val initialName = task.name ?: ""
                        if (!state.isFocused && taskName != initialName) {
                            if (taskName.isBlank()) taskName = initialName
                            else {
                                val newName = onChangeTaskName(taskName)
                                taskName = newName
                            }
                        }
                    }
            )
            if (taskName.isBlank() || taskName == task.name)
                Spacer(modifier = Modifier.width(48.dp))
            else {
                IconButton(onClick = {
                    val newName = onChangeTaskName(taskName)
                    taskName = newName
                }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

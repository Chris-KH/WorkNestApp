package com.apcs.worknestapp.ui.components.notedetail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.note.Checklist
import com.apcs.worknestapp.ui.components.inputfield.CustomTextField
import com.apcs.worknestapp.ui.theme.Roboto

@Composable
fun ChecklistItem(
    checklist: Checklist,
    modifier: Modifier = Modifier,
    onChangeChecklistName: (String) -> Unit,
    onDeleteChecklist: () -> Unit,
) {
    var checklistName by remember { mutableStateOf(checklist.name ?: "xxx") }
    var isCollapsed by remember { mutableStateOf(false) }
    var hideCompleted by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val focusRequester = remember { FocusRequester() }
            var showDropdownMenu by remember { mutableStateOf(false) }
            val rotation by animateFloatAsState(
                targetValue = if (isCollapsed) 0f else 180f, label = "arrowRotation"
            )
            val textStyle = TextStyle(
                fontSize = 15.sp, lineHeight = 16.sp, letterSpacing = (0.25).sp,
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
                    .focusRequester(focusRequester)
                    .onFocusChanged { state ->
                        val initialName = checklist.name ?: ""
                        if (!state.isFocused && checklistName != initialName) {
                            if (checklistName.isBlank()) checklistName = initialName
                            else onChangeChecklistName(checklistName)
                        }
                    }
            )
            IconButton(onClick = { isCollapsed = !isCollapsed }) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = if (isCollapsed) "Expand" else "Collapse",
                    modifier = Modifier.rotate(rotation)
                )
            }
            IconButton(onClick = { showDropdownMenu = true }) {
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
                        fontSize = 15.sp, lineHeight = 16.sp,
                        fontFamily = Roboto, fontWeight = FontWeight.Normal,
                    )
                    val horizontalPadding = 20.dp
                    val iconSize = 24.dp

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
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = {
                            Text(text = "Delete", style = dropdownTextStyle)
                        },
                        onClick = {
                            showDropdownMenu = false
                            onDeleteChecklist()
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
    }
}

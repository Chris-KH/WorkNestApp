package com.apcs.worknestapp.ui.screens.note

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.theme.Roboto

enum class NoteSortBy {
    NEWEST,
    OLDEST,
    ALPHABETICAL,
}

@Composable
fun NoteDropdownActions(
    expanded: Boolean,
    isNoteEmpty: Boolean,
    onDismissRequest: () -> Unit,
    onChangeBackground: () -> Unit,
    onSort: (NoteSortBy) -> Unit,
    onViewArchive: () -> Unit,
    onArchiveCompletedNotes: () -> Unit,
    onArchiveAllNotes: () -> Unit,
    onDeleteAllNotes: () -> Unit,
) {
    var showSortDropdown by remember { mutableStateOf(false) }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.widthIn(min = 240.dp)
    ) {
        val dropdownTextStyle = TextStyle(
            fontSize = 15.sp, lineHeight = 16.sp,
            fontFamily = Roboto, fontWeight = FontWeight.Normal,
        )
        val horizontalPadding = 24.dp
        val iconSize = 24.dp

        AnimatedContent(
            targetState = showSortDropdown,
            label = "Dropdown switch animation"
        ) {
            Column {
                if (!it) {
                    // GROUP 1
                    DropdownMenuItem(
                        text = {
                            Text(text = "Change background", style = dropdownTextStyle)
                        },
                        onClick = { onChangeBackground() },
                        trailingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.outline_palette),
                                contentDescription = "Change background",
                                modifier = Modifier.size(iconSize),
                            )
                        },
                        contentPadding = PaddingValues(horizontal = horizontalPadding)
                    )
                    if (!isNoteEmpty) {
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Text(text = "Sort", style = dropdownTextStyle)
                            },
                            onClick = { showSortDropdown = true },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.outline_sort),
                                    contentDescription = "Sort notes by",
                                    modifier = Modifier.size(iconSize),
                                )
                            },
                            contentPadding = PaddingValues(horizontal = horizontalPadding)
                        )
                    }

                    HorizontalDivider(
                        thickness = 8.dp,
                        color = MaterialTheme.colorScheme.surfaceContainer
                    )

                    // GROUP 2
                    DropdownMenuItem(
                        text = {
                            Text(text = "View archive", style = dropdownTextStyle)
                        },
                        trailingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.outline_archive),
                                contentDescription = "Change background",
                                modifier = Modifier.size(iconSize),
                            )
                        },
                        onClick = onViewArchive,
                        contentPadding = PaddingValues(horizontal = horizontalPadding)
                    )
                    if (!isNoteEmpty) {
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Text(text = "Archive completed", style = dropdownTextStyle)
                            },
                            onClick = onArchiveCompletedNotes,
                            contentPadding = PaddingValues(horizontal = horizontalPadding)
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Text(text = "Archive all notes", style = dropdownTextStyle)
                            },
                            onClick = onArchiveAllNotes,
                            contentPadding = PaddingValues(horizontal = horizontalPadding)
                        )
                    }

                    if (!isNoteEmpty) {
                        HorizontalDivider(
                            thickness = 8.dp,
                            color = MaterialTheme.colorScheme.surfaceContainer
                        )

                        // GROUP 3
                        DropdownMenuItem(
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.error,
                                trailingIconColor = MaterialTheme.colorScheme.error,
                            ),
                            text = {
                                Text(text = "Delete all notes", style = dropdownTextStyle)
                            },
                            onClick = onDeleteAllNotes,
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.outline_trash),
                                    contentDescription = "Delete all",
                                    modifier = Modifier.size(iconSize),
                                )
                            },
                            contentPadding = PaddingValues(horizontal = horizontalPadding)
                        )
                    }
                } else {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Back",
                                fontSize = 16.sp, lineHeight = 16.sp,
                                fontFamily = Roboto, fontWeight = FontWeight.Medium,
                            )
                        },
                        onClick = { showSortDropdown = false },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Back",
                            )
                        },
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = {
                            Text(text = "Newest first", style = dropdownTextStyle)
                        },
                        onClick = {
                            onSort(NoteSortBy.NEWEST)
                            onDismissRequest()
                        },
                        contentPadding = PaddingValues(horizontal = horizontalPadding)
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = {
                            Text(text = "Oldest first", style = dropdownTextStyle)
                        },
                        onClick = {
                            onSort(NoteSortBy.OLDEST)
                            onDismissRequest()
                        },
                        contentPadding = PaddingValues(horizontal = horizontalPadding)
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = {
                            Text(text = "Alphabetical", style = dropdownTextStyle)
                        },
                        onClick = {
                            onSort(NoteSortBy.ALPHABETICAL)
                            onDismissRequest()
                        },
                        contentPadding = PaddingValues(horizontal = horizontalPadding)
                    )
                }
            }
        }
    }
}

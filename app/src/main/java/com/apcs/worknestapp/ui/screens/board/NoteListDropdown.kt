package com.apcs.worknestapp.ui.screens.board

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.theme.Roboto

@Composable
fun NoteListDropdown(
    expanded: Boolean,
    isNoteEmpty: Boolean,
    onDismissRequest: () -> Unit,
    onArchiveCompletedNotes: () -> Unit,
    onArchiveAllNotes: () -> Unit,
    onArchiveNoteList: () -> Unit,
    onDeleteAllNotes: () -> Unit,
    onDeleteNoteList: () -> Unit,
) {
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

        // GROUP 1
        if (!isNoteEmpty) {
            DropdownMenuItem(
                text = {
                    Text(text = "Archive completed", style = dropdownTextStyle)
                },
                onClick = onArchiveCompletedNotes,
                trailingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.outline_archive),
                        contentDescription = "Archive completed",
                        modifier = Modifier.size(iconSize),
                    )
                },
                contentPadding = PaddingValues(horizontal = horizontalPadding)
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = {
                    Text(text = "Archive all notes", style = dropdownTextStyle)
                },
                onClick = onArchiveAllNotes,
                trailingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.outline_archive),
                        contentDescription = "Archive all notes",
                        modifier = Modifier.size(iconSize),
                    )
                },
                contentPadding = PaddingValues(horizontal = horizontalPadding)
            )
            HorizontalDivider()
        }
        DropdownMenuItem(
            text = {
                Text(text = "Archive list", style = dropdownTextStyle)
            },
            onClick = onArchiveNoteList,
            trailingIcon = {
                Icon(
                    painter = painterResource(R.drawable.outline_archive),
                    contentDescription = "Archive list",
                    modifier = Modifier.size(iconSize),
                )
            },
            contentPadding = PaddingValues(horizontal = horizontalPadding)
        )
        HorizontalDivider(
            thickness = 8.dp,
            color = MaterialTheme.colorScheme.surfaceContainer
        )


        // GROUP 2
        if (!isNoteEmpty) {
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
        DropdownMenuItem(
            colors = MenuDefaults.itemColors(
                textColor = MaterialTheme.colorScheme.error,
                trailingIconColor = MaterialTheme.colorScheme.error,
            ),
            text = {
                Text(text = "Delete note list", style = dropdownTextStyle)
            },
            onClick = onDeleteNoteList,
            trailingIcon = {
                Icon(
                    painter = painterResource(R.drawable.outline_trash),
                    contentDescription = "Delete note list",
                    modifier = Modifier.size(iconSize),
                )
            },
            contentPadding = PaddingValues(horizontal = horizontalPadding)
        )
    }
}

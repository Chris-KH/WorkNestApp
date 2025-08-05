package com.apcs.worknestapp.ui.screens.note

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
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
fun NoteDropdownActions(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onChangeBackground: () -> Unit,
    onSort: () -> Unit,
    onViewArchive: () -> Unit,
    onArchiveCompletedNotes: () -> Unit,
    onArchiveAllNotes: () -> Unit,
    onDeleteAllNotes: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        val dropdownTextStyle = TextStyle(
            fontSize = 15.sp, lineHeight = 16.sp,
            fontFamily = Roboto, fontWeight = FontWeight.Normal,
        )
        val horizontalPadding = 24.dp
        val iconSize = 26.dp

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
        HorizontalDivider()
        DropdownMenuItem(
            text = {
                Text(text = "Sort", style = dropdownTextStyle)
            },
            onClick = { onSort() },
            trailingIcon = {
                Icon(
                    painter = painterResource(R.drawable.outline_sort),
                    contentDescription = "Sort notes by",
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
}

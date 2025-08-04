package com.apcs.worknestapp.ui.screens.note_detail

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.R

@Composable
fun NoteDetailActions(
    onEditClick: () -> Unit,
    onDeleteAllClick: () -> Unit,
    modifier: Modifier = Modifier, // Standard practice to accept a modifier
) {
    var showSubMenu by remember { mutableStateOf(false) }
    IconButton(onClick = { showSubMenu = true }, modifier = modifier) {
        Icon(
            painter = painterResource(R.drawable.symbol_three_dot),
            contentDescription = "More options",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(24.dp)
                .rotate(-90f)
        )
    }
    DropdownMenu(
        expanded = showSubMenu,
        onDismissRequest = { showSubMenu = false },
        containerColor = MaterialTheme.colorScheme.surface,
        shadowElevation = 32.dp,
        shape = RoundedCornerShape(25f),
        modifier = Modifier.widthIn(min = 160.dp),
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    text = "Edit",
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            },
            onClick = {
                onEditClick()
                showSubMenu = false
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(24.dp),
                )
            }
        )
        DropdownMenuItem(
            text = {
                Text(
                    text = "Delete all",
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            },
            onClick = {
                onDeleteAllClick()
                showSubMenu = false
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete All",
                    modifier = Modifier.size(24.dp),
                )
            }
        )
    }
}

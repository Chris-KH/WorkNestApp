package com.apcs.worknestapp.ui.components.notedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NoteModalBottomTopBar(
    title: String,
    onClose: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 8.dp)
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier.align(alignment = Alignment.CenterStart),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close modal",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier,
            )
        }
        Box(modifier = Modifier.align(alignment = Alignment.Center)) {
            Text(
                text = title,
                fontSize = 16.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        IconButton(
            onClick = onSave,
            modifier = Modifier.align(alignment = Alignment.CenterEnd),
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Save change",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier,
            )
        }
    }
}

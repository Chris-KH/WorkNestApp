package com.apcs.worknestapp.ui.screens.note

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NoteItem(
    note: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier

) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.5f
                )
            )
            .clickable(onClick = onClick)
            .padding(all = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = note,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

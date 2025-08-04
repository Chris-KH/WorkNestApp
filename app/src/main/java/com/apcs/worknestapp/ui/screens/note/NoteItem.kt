package com.apcs.worknestapp.ui.screens.note

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.data.remote.note.Note
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.theme.success

@Composable
fun NoteItem(
    note: Note,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(
                if (note.completed == null || !note.completed) R.drawable.outline_circle
                else R.drawable.fill_checkbox
            ),
            tint = if (note.completed == null || !note.completed) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.success,
            contentDescription = null,
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .let {
                    if (note.completed == true) return@let it.background(MaterialTheme.colorScheme.onSurface)
                    return@let it
                }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = note.name ?: "",
            fontSize = 14.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
    }
}

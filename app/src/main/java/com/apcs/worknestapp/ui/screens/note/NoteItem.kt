package com.apcs.worknestapp.ui.screens.note

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.note.Note
import com.apcs.worknestapp.ui.components.RotatingIcon
import com.apcs.worknestapp.ui.theme.success

@Composable
fun NoteItem(
    note: Note,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    onCompleteClick: () -> Unit,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(8.dp, MaterialTheme.colorScheme.surfaceVariant, shape)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.Top
    ) {
        val fontSize = 15.sp
        val lineHeight = 16.sp
        val iconSize = with(LocalDensity.current) { fontSize.toDp() + 2.dp }

        Icon(
            painter = painterResource(
                if (note.completed == null || !note.completed) R.drawable.outline_circle
                else R.drawable.fill_checkbox
            ),
            tint = if (note.completed == null || !note.completed) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.success,
            contentDescription = null,
            modifier = Modifier
                .clickable(onClick = onCompleteClick)
                .size(iconSize)
                .clip(CircleShape)
                .let {
                    if (note.completed == true) return@let it.background(MaterialTheme.colorScheme.onSurface)
                    return@let it
                }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = note.name ?: "",
            fontSize = fontSize,
            lineHeight = lineHeight,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        if (note.isLoading == true) {
            Spacer(modifier = Modifier.width(6.dp))
            RotatingIcon(
                painter = painterResource(R.drawable.loading_icon_5),
                contentDescription = "Adding note",
                modifier = Modifier
                    .size(iconSize)
                    .aspectRatio(1f),
            )
        } else Spacer(modifier = Modifier.width(6.dp + iconSize))
    }
}

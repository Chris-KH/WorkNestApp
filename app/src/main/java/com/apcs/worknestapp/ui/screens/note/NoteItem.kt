package com.apcs.worknestapp.ui.screens.note

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.note.Note
import com.apcs.worknestapp.ui.components.RotatingIcon
import com.apcs.worknestapp.ui.theme.success
import com.apcs.worknestapp.utils.ColorUtils

@Composable
fun NoteItem(
    note: Note,
    selectedMode: Boolean,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    onCompleteClick: () -> Unit,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    val coverColor = note.cover?.let { ColorUtils.safeParse(it) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape)
                .border(
                    width = 8.dp,
                    color = if (isSelected && selectedMode) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    shape = shape
                )
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
        ) {
            if (coverColor != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(coverColor)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalAlignment = Alignment.Top
            ) {
                val fontSize = 15.sp
                val lineHeight = 15.sp
                val iconSize = with(LocalDensity.current) { fontSize.toDp() + 2.dp }

                AnimatedVisibility(
                    visible = !selectedMode,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                                    if (note.completed == true) it.background(MaterialTheme.colorScheme.onSurface)
                                    else it
                                }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }

                Text(
                    text = note.name ?: "",
                    fontSize = fontSize,
                    lineHeight = lineHeight,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                if (note.isLoading == true) {
                    Spacer(modifier = Modifier.width(4.dp))
                    RotatingIcon(
                        painter = painterResource(R.drawable.loading_icon_5),
                        contentDescription = "Adding note",
                        modifier = Modifier
                            .size(iconSize)
                            .aspectRatio(1f),
                    )
                } else Spacer(modifier = Modifier.width(4.dp + iconSize))
            }
        }
        if (isSelected && selectedMode) {
            val boxSize = 52.dp
            val iconSize = 20.dp

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(boxSize),
                contentAlignment = Alignment.Center
            ) {
                val color = MaterialTheme.colorScheme.primary

                Canvas(modifier = Modifier.matchParentSize()) {
                    val path = Path().apply {
                        moveTo(size.width, 0f)
                        lineTo(size.width, size.height)
                        lineTo(0f, 0f)
                        close()
                    }
                    drawPath(path, color = color)
                }

                Icon(
                    painter = painterResource(R.drawable.fill_check),
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding((boxSize / 2) - iconSize)
                        .size(iconSize)
                        .zIndex(10f)
                )
            }
        }
    }

}

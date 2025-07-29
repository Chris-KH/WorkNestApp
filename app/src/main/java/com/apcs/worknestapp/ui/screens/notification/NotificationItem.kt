package com.apcs.worknestapp.ui.screens.notification

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.notification.Notification
import com.apcs.worknestapp.domain.logic.DateFormater

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationItem(
    notification: Notification,
    onDelete: (String?) -> Unit,
    onClick: (String) -> Unit = {},
) {
    var showModal by remember { mutableStateOf(false) }

    val formatedDate =
        if (notification.createdAt == null) ""
        else DateFormater.format(
            notification.createdAt,
            formatString = "dd MMMM, yyyy 'at' HH:mm"
        )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
            .clickable(onClick = {
                if (notification.docId != null) onClick(notification.docId)
            }),
    ) {
        if (showModal) {
            ModalBottomSheet(
                onDismissRequest = { showModal = false },
                contentColor = MaterialTheme.colorScheme.onSurface,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(onClick = {
                            onDelete(notification.docId)
                            showModal = false
                        })
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.fill_delete),
                        contentDescription = "Delete notification",
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape,
                            )
                            .padding(10.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Delete this notification",
                        fontSize = 14.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Normal,
                    )
                }
            }
        }

        if (notification.read == null || !notification.read) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .zIndex(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .offset(x = 4.dp, y = (-4).dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                        )
                        .clip(CircleShape)
                        .align(alignment = Alignment.TopEnd)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp)
                .zIndex(10f),
        ) {
            val spacerWidth = 10.dp

            Image(
                painter = painterResource(R.drawable.sticker_notification),
                contentDescription = "Notification",
                modifier = Modifier.size(64.dp),
            )
            Spacer(modifier = Modifier.width(spacerWidth))
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = notification.title ?: "",
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    letterSpacing = 0.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.message ?: "",
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    letterSpacing = 0.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatedDate,
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    letterSpacing = 0.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            Spacer(modifier = Modifier.width(spacerWidth))
            IconButton(
                onClick = { showModal = true },
                modifier = Modifier.align(Alignment.Top)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    modifier = Modifier.rotate(90f)
                )
            }
        }
    }
}

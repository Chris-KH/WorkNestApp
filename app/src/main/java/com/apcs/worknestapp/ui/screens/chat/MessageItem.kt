package com.apcs.worknestapp.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.message.Conservation
import com.apcs.worknestapp.data.remote.message.Message
import com.apcs.worknestapp.domain.logic.DateFormater
import com.apcs.worknestapp.domain.usecase.AppDefault
import com.apcs.worknestapp.ui.components.RotatingIcon
import com.apcs.worknestapp.ui.theme.Roboto

@Composable
fun MessageItem(
    message: Message,
    conservation: Conservation,
    isMyMessage: Boolean,
    showSentDate: Boolean,
    isLastMessage: Boolean,
    modifier: Modifier = Modifier,
) {
    var isShowDate by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        if (showSentDate || isShowDate) {
            Text(
                text =
                    if (message.createdAt != null) DateFormater.formatMessageTime(message.createdAt)
                    else "...",
                fontSize = 12.sp,
                lineHeight = 12.sp,
                fontFamily = Roboto,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        Row(
            horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        ) {
            Column(
                horizontalAlignment = if (isMyMessage) Alignment.End else Alignment.Start,
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .fillMaxWidth(0.85f)
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    if (!isMyMessage) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(conservation.userData.avatar ?: AppDefault.AVATAR)
                                .crossfade(true)
                                .build(),
                            placeholder = painterResource(R.drawable.fade_avatar_fallback),
                            error = painterResource(R.drawable.fade_avatar_fallback),
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            filterQuality = FilterQuality.Low,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clip(RoundedCornerShape(10.dp))
                            .combinedClickable(
                                onClick = { isShowDate = !isShowDate },
                                onLongClick = {},
                            )
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = message.content ?: "Hehe",
                            fontSize = 14.sp,
                            lineHeight = 15.sp,
                            fontFamily = Roboto,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (isMyMessage) {
                    val fontSize = 12.sp
                    val textStyle = TextStyle(
                        fontSize = fontSize, lineHeight = fontSize,
                        fontFamily = Roboto, fontWeight = FontWeight.Medium,
                    )
                    if (message.isSentSuccess == false) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(with(LocalDensity.current) { fontSize.toDp() }),
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Failed",
                                style = textStyle,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    } else if (message.isSending == true) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RotatingIcon(
                                painter = painterResource(R.drawable.loading_icon_5),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(with(LocalDensity.current) { fontSize.toDp() }),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sending",
                                style = textStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else if (isLastMessage && message.isSending == false && message.isSentSuccess == true) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(with(LocalDensity.current) { fontSize.toDp() }),
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Sent",
                                style = textStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.apcs.worknestapp.ui.screens.contact

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.message.Conservation
import com.apcs.worknestapp.data.remote.user.User
import com.apcs.worknestapp.domain.logic.DateFormater
import com.apcs.worknestapp.ui.theme.Inter
import com.apcs.worknestapp.ui.theme.Roboto
import com.google.firebase.Timestamp
import java.util.Date

@Composable
fun ConservationItem(
    conservation: Conservation,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = {},
                indication = ripple(color = MaterialTheme.colorScheme.primary),
                interactionSource = interactionSource,
            )
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(null)
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.fade_avatar_fallback),
            error = painterResource(R.drawable.fade_avatar_fallback),
            contentDescription = "Preview avatar",
            contentScale = ContentScale.Crop,
            filterQuality = FilterQuality.Low,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            val textColor =
                if (conservation.seen == true) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onBackground
            val fontWeight = if (conservation.seen == true) FontWeight.Normal else FontWeight.Medium
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Name here",
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    fontFamily = Roboto,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = fontWeight,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (conservation.lastTime == null) ""
                    else DateFormater.formatConversationTime(conservation.lastTime),
                    fontSize = 11.sp,
                    lineHeight = 11.sp,
                    fontFamily = Roboto,
                    color = textColor,
                    fontWeight = fontWeight,
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = conservation.lastContent ?: "...",
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    fontFamily = Roboto,
                    color = textColor,
                    fontWeight = fontWeight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                if (conservation.seen != true) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }
            }
        }
    }
}

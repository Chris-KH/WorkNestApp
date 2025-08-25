package com.apcs.worknestapp.ui.screens.contact

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.user.User
import com.apcs.worknestapp.ui.theme.Roboto

@Composable
fun FriendItem(
    friend: User,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    val horizontalPadding = 16.dp
    val verticalPadding = 12.dp
    val spacerWidth = 16.dp
    val avatarSize = 52.dp

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
            .padding(vertical = verticalPadding, horizontal = horizontalPadding),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(friend.avatar)
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.fade_avatar_fallback),
            error = painterResource(R.drawable.fade_avatar_fallback),
            contentDescription = "Preview avatar",
            contentScale = ContentScale.Crop,
            filterQuality = FilterQuality.Low,
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape),
        )
        Spacer(modifier = Modifier.width(spacerWidth))
        Text(
            text = friend.name ?: "",
            fontSize = 14.sp,
            lineHeight = 14.sp,
            fontFamily = Roboto,
            fontWeight = FontWeight.Medium,
        )
    }
}

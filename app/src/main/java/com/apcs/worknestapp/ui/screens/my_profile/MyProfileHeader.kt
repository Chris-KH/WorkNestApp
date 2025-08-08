package com.apcs.worknestapp.ui.screens.my_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.ui.components.AvatarPicker

@Composable
fun MyProfileHeader(
    userId: String?,
    userName: String?,
    userEmail: String?,
    imageUrl: String?,
    avatarSize: Dp,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(avatarSize + 16.dp)
                .background(MaterialTheme.colorScheme.background, CircleShape)
        ) {
            AvatarPicker(
                userId = userId,
                imageUrl = imageUrl,
                snackbarHost = snackbarHost,
                modifier = Modifier
                    .align(alignment = Alignment.Center)
                    .size(avatarSize)
                    .clip(CircleShape),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = userName ?: "Anonymous",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = userEmail ?: "",
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

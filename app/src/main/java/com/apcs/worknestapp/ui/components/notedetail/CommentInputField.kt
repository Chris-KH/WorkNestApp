package com.apcs.worknestapp.ui.components.notedetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.apcs.worknestapp.LocalAuthViewModel
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.components.inputfield.CustomTextField
import com.apcs.worknestapp.ui.theme.Roboto

@Composable
fun CommentInputSection(
    commentText: String,
    onCommentTextChange: (String) -> Unit,
    onPostComment: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val authViewModel = LocalAuthViewModel.current
    val userProfile by authViewModel.profile.collectAsState()

    Box(
        modifier = modifier,
    ) {
        HorizontalDivider(
            modifier = Modifier.align(alignment = Alignment.TopCenter)
        )
        Row(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(userProfile?.avatar)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.fade_avatar_fallback),
                error = painterResource(R.drawable.fade_avatar_fallback),
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                filterQuality = FilterQuality.Low,
                modifier = Modifier
                    .size(44.dp)
                    .border(
                        width = 1.dp,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            CustomTextField(
                value = commentText,
                onValueChange = onCommentTextChange,
                textStyle = TextStyle(
                    fontSize = 15.sp, lineHeight = 15.sp, letterSpacing = 0.sp,
                    fontFamily = Roboto, fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                placeholder = {
                    Text(
                        text = "Add a comment...",
                        fontSize = 15.sp, lineHeight = 15.sp, letterSpacing = 0.sp,
                        fontFamily = Roboto, fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    width = (0.8).dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                ),
                maxLines = 4
            )
            IconButton(
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = Color.Unspecified,
                ),
                onClick = {
//                userProfile?.let { profile ->
//                    if (commentText.isNotBlank()) {
//                        val newComment = Comment(
//                            id = UUID.randomUUID().toString(),
//                            text = commentText,
//                            author = User(
//                                name = profile.name ?: "Anonymous",
//                                avatarUrl = profile.avatar ?: ""
//                            ),
//                            timestamp = Date()
//                        )
//                        commentList = commentList + newComment
//                        commentText = ""
//                        focusManager.clearFocus()
//                    }
//                } ?: run { }
                    onPostComment()
                },
                enabled = commentText.isNotBlank()
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, "Post comment")
            }
        }
    }
}

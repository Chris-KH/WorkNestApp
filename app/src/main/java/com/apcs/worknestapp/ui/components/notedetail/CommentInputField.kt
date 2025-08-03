package com.apcs.worknestapp.ui.components.notedetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.ui.components.inputfield.CustomTextField
import com.apcs.worknestapp.ui.theme.Roboto

@Composable
fun CommentInputSection(
    commentText: String,
    onCommentTextChange: (String) -> Unit,
    onPostComment: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CustomTextField(
            value = commentText,
            onValueChange = onCommentTextChange,
            textStyle = TextStyle(
                fontSize = 15.sp, lineHeight = 15.sp, letterSpacing = 0.sp,
                fontFamily = Roboto, fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground,
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
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
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
            onClick = onPostComment,
            enabled = commentText.isNotBlank()
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, "Post comment")
        }
    }
}

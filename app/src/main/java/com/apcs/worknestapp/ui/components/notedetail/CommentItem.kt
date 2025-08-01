package com.apcs.worknestapp.ui.components.notedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


data class Comment(
    val id: String,
    val text: String,
    val author: User, // Changed from 'user' to 'author' for clarity
    val timestamp: Date,
    val replies: List<Comment> = emptyList(),
    //TODO: Mock data class. implemment model.
)


@Composable
fun CommentItem(comment: Comment, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            CommentAuthorHeader(author = comment.author)

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = SimpleDateFormat(
                    "MMM d, yyyy h:mm a",
                    Locale.getDefault()
                ).format(comment.timestamp), color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = comment.text, style = MaterialTheme.typography.bodyMedium)

//            if (comment.replies.isNotEmpty()) {
//                // ... (reply logic remains the same) ...
//                // TODO:
//            }
        }
    }
}

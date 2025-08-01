package com.apcs.worknestapp.ui.components.notedetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.apcs.worknestapp.R



data class User(
    //val id: String,
    val name: String?,
    val avatarUrl: String?,
    //val email: String?,
    //val pronouns: String?
    // TODO: This is a mock dataclass. implement real thing here.
    // val noteData = viewModel.getNoteById(noteId)
    // Display noteData...
)

@Composable
fun CommentAuthorHeader(
    author: User,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = author.avatarUrl,
                placeholder = painterResource(R.drawable.fade_avatar_fallback),
                error = painterResource(R.drawable.fade_avatar_fallback)
            ),
            contentDescription = "${author.name ?: "User"}'s profile picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = author.name ?: "Anonymous",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
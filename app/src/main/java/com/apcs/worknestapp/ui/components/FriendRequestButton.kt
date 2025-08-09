package com.apcs.worknestapp.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.user.Friendship
import com.apcs.worknestapp.ui.theme.Roboto
import com.apcs.worknestapp.ui.theme.onSuccess
import com.apcs.worknestapp.ui.theme.success

@Composable
fun FriendRequestButton(
    targetUserId: String,
    friendship: Friendship?,
    onAdd: (userId: String) -> Unit,
    onCancel: (docId: String) -> Unit,
    onRemove: (docId: String) -> Unit,
    textStyle: TextStyle = TextStyle(
        fontSize = 16.sp, lineHeight = 16.sp, letterSpacing = 0.sp,
        fontWeight = FontWeight.SemiBold, fontFamily = Roboto,
    ),
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (friendship == null) {
        Button(
            onClick = { onAdd(targetUserId) },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
            )
        ) {
            Icon(
                painter = painterResource(R.drawable.outline_add_user),
                contentDescription = "Add friend",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Add", style = textStyle)
        }
    } else if (friendship.status == "accepted") {
        Button(
            onClick = {
                if (friendship.docId != null) onCancel(friendship.docId)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurface,
            )
        ) {
            Icon(
                painter = painterResource(R.drawable.outline_active_user),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Friend", style = textStyle)
        }
    } else if (friendship.receiverId == targetUserId) {
        Button(
            onClick = { if (friendship.docId != null) onCancel(friendship.docId) },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            )
        ) {
            Icon(
                painter = painterResource(R.drawable.outline_remove_user),
                contentDescription = "Remove friend",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Cancel", style = textStyle)
        }
    } else {
        Button(
            onClick = { showConfirmDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.success,
                contentColor = MaterialTheme.colorScheme.onSuccess,
            )
        ) {
            Icon(
                painter = painterResource(R.drawable.outline_active_user),
                contentDescription = "Accept friend",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Confirm", style = textStyle)
        }
    }
}

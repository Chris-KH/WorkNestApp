package com.apcs.worknestapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendRequestButton(
    targetUserId: String,
    friendship: Friendship?,
    onAdd: (userId: String) -> Unit,
    onConfirm: (docId: String) -> Unit,
    onCancel: (docId: String) -> Unit,
    onRemove: (docId: String) -> Unit,
    textStyle: TextStyle = TextStyle(
        fontSize = 16.sp, lineHeight = 16.sp, letterSpacing = 0.sp,
        fontWeight = FontWeight.SemiBold, fontFamily = Roboto,
    ),
) {
    var showRespondModalBottom by remember { mutableStateOf(false) }
    var showFriendModalBottom by remember { mutableStateOf(false) }
    val modalContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
    val modalContentColor = MaterialTheme.colorScheme.onSurface
    val listItemColor = ListItemDefaults.colors(
        containerColor = modalContainerColor,
        headlineColor = modalContentColor,
        leadingIconColor = modalContentColor,
        trailingIconColor = modalContentColor,
    )
    val headlineTextStyle = TextStyle(
        fontSize = 16.sp, lineHeight = 16.sp, letterSpacing = 0.sp,
        fontFamily = Roboto, fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
    )

    fun onDismissModal() {
        showRespondModalBottom = false
        showFriendModalBottom = false
    }

    if (friendship != null && (showRespondModalBottom || showFriendModalBottom)) {
        ModalBottomSheet(
            contentColor = modalContentColor,
            containerColor = modalContainerColor,
            onDismissRequest = { onDismissModal() },
        ) {
            if (showRespondModalBottom) {
                ListItem(
                    headlineContent = { Text(text = "Confirm", style = headlineTextStyle) },
                    leadingContent = {
                        IconWithBackground(
                            painter = painterResource(R.drawable.outline_active_user),
                            contentDescription = "Confirm friend request",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    colors = listItemColor,
                    modifier = Modifier.clickable(
                        onClick = {
                            if (friendship.docId != null) onConfirm(friendship.docId)
                            onDismissModal()
                        }
                    )
                )
                ListItem(
                    headlineContent = { Text(text = "Deny", style = headlineTextStyle) },
                    leadingContent = {
                        IconWithBackground(
                            painter = painterResource(R.drawable.outline_remove_user),
                            contentDescription = "Deny friend request",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    colors = listItemColor,
                    modifier = Modifier.clickable(
                        onClick = {
                            if (friendship.docId != null) onRemove(friendship.docId)
                            onDismissModal()
                        }
                    )
                )
            } else {
                ListItem(
                    headlineContent = { Text(text = "Delete", style = headlineTextStyle) },
                    leadingContent = {
                        IconWithBackground(
                            painter = painterResource(R.drawable.outline_remove_user),
                            contentDescription = "Delete friend",
                            modifier = Modifier.size(24.dp)

                        )
                    },
                    colors = listItemColor,
                    modifier = Modifier.clickable(
                        onClick = {
                            if (friendship.docId != null) onRemove(friendship.docId)
                            onDismissModal()
                        }
                    )
                )
            }
        }
    }

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
            onClick = { showFriendModalBottom = true },
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
    } else if (friendship.receiver?.id == targetUserId) {
        Button(
            onClick = { if (friendship.docId != null) onCancel(friendship.docId) },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            )
        ) {
            Icon(
                painter = painterResource(R.drawable.outline_remove_user),
                contentDescription = "Cancel friend request",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Cancel", style = textStyle)
        }
    } else {
        Button(
            onClick = { showRespondModalBottom = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.success,
                contentColor = MaterialTheme.colorScheme.onSuccess,
            )
        ) {
            Icon(
                painter = painterResource(R.drawable.outline_active_user),
                contentDescription = "Respond friend request",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Respond", style = textStyle)
        }
    }
}

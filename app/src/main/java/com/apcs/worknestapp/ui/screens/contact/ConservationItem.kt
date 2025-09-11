package com.apcs.worknestapp.ui.screens.contact

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.message.Conservation
import com.apcs.worknestapp.domain.logic.DateFormater
import com.apcs.worknestapp.domain.usecase.AppDefault
import com.apcs.worknestapp.ui.components.ConfirmDialog
import com.apcs.worknestapp.ui.theme.Roboto
import com.apcs.worknestapp.ui.theme.success
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ConservationItem(
    conservation: Conservation,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onMarkSeenState: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    val authId = FirebaseAuth.getInstance().currentUser?.uid
    val isSeen = (authId == conservation.sender?.id && conservation.senderSeen == true)
            || (authId != conservation.sender?.id && conservation.receiverSeen == true)

    var showDropdown by remember { mutableStateOf(false) }
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    val horizontalPadding = 16.dp
    val verticalPadding = 12.dp
    val spacerWidth = 12.dp
    val avatarSize = 52.dp

    //var isPin by remember { mutableStateOf(true) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showDropdown = true },
                indication = ripple(color = MaterialTheme.colorScheme.primary),
                interactionSource = interactionSource,
            )
            .fillMaxWidth()
            .padding(vertical = verticalPadding, horizontal = horizontalPadding),
    ) {
        if (showConfirmDeleteDialog) {
            ConfirmDialog(
                title = "Delete chat with ${conservation.userData.name}",
                message = "Are you sure you want to delete this conversation? This action cannot be undone.",
                onDismissRequest = { showConfirmDeleteDialog = false },
                confirmText = "Delete",
                cancelText = "Cancel",
                onConfirm = {
                    showConfirmDeleteDialog = false
                    onDelete()
                },
                onCancel = { showConfirmDeleteDialog = false }
            )
        }

        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.widthIn(min = 180.dp),
        )
        {
            val dropdownTextStyle = TextStyle(
                fontSize = 15.sp, lineHeight = 16.sp,
                fontFamily = Roboto, fontWeight = FontWeight.Normal,
            )
            val horizontalPadding = 16.dp
            val iconSize = 24.dp

            DropdownMenuItem(
                text = {
                    Text(
                        text = "Mark as ${if (isSeen) "unread" else "read"}",
                        style = dropdownTextStyle,
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(
                            if (isSeen) R.drawable.outline_unread
                            else R.drawable.outline_read
                        ),
                        contentDescription = "Mark ${if (isSeen) "unread" else "read"}",
                        modifier = Modifier.size(iconSize)
                    )
                },
                onClick = {
                    onMarkSeenState(!isSeen)
                    showDropdown = false
                },
                contentPadding = PaddingValues(horizontal = horizontalPadding)
            )
            //TODO
//            DropdownMenuItem(
//                text = {
//                    Text(
//                        text = if (isPin) "Unpin" else "Pin",
//                        style = dropdownTextStyle,
//                    )
//                },
//                leadingIcon = {
//                    Icon(
//                        painter = painterResource(
//                            if (isPin) R.drawable.outline_unpin
//                            else R.drawable.outline_pin
//                        ),
//                        contentDescription = if (isPin) "Unpin" else "Pin",
//                        modifier = Modifier.size(iconSize)
//                    )
//                },
//                onClick = {
//                    //TODO: Future feature, but not now:))
//                    showDropdown = false
//                },
//                contentPadding = PaddingValues(horizontal = horizontalPadding)
//            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Delete",
                        style = dropdownTextStyle,
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.outline_trash),
                        contentDescription = "Delete conservation",
                        modifier = Modifier.size(iconSize)
                    )
                },
                onClick = {
                    showDropdown = false
                    showConfirmDeleteDialog = true
                },
                colors = MenuDefaults.itemColors(
                    textColor = MaterialTheme.colorScheme.error,
                    leadingIconColor = MaterialTheme.colorScheme.error,
                    trailingIconColor = MaterialTheme.colorScheme.error,
                ),
                contentPadding = PaddingValues(horizontal = horizontalPadding)
            )
        }
        Box(modifier = Modifier.wrapContentSize()) {
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
                    .size(avatarSize)
                    .clip(CircleShape),
            )
            if (conservation.userData.online == true) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(MaterialTheme.colorScheme.success, CircleShape)
                        .align(alignment = Alignment.BottomEnd)
                        .zIndex(10f)
                )
            }
        }
        Spacer(modifier = Modifier.width(spacerWidth))
        Column(modifier = Modifier.weight(1f)) {
            val contentTextStyle = TextStyle(
                fontSize = 12.sp, lineHeight = 12.sp, fontFamily = Roboto,
                color =
                    if (isSeen) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onBackground,
                fontWeight =
                    if (isSeen) FontWeight.Normal
                    else FontWeight.Medium
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = conservation.userData.name ?: AppDefault.USER_NAME,
                    fontSize = 15.sp,
                    lineHeight = 15.sp,
                    fontFamily = Roboto,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (conservation.lastTime == null) ""
                    else DateFormater.formatConversationTime(conservation.lastTime),
                    style = contentTextStyle,
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = conservation.lastContent ?: "...",
                    style = contentTextStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                if (!isSeen) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }
            }
        }
    }
    HorizontalDivider(
        thickness = (0.75).dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = horizontalPadding + avatarSize + spacerWidth)
    )
}

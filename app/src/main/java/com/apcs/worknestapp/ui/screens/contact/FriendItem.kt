package com.apcs.worknestapp.ui.screens.contact

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.draw.rotate
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
import com.apcs.worknestapp.data.remote.user.User
import com.apcs.worknestapp.ui.components.ConfirmDialog
import com.apcs.worknestapp.ui.theme.Roboto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendItem(
    friend: User,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onMessage: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    var showDialog by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showDropdownMenu by remember { mutableStateOf(false) }

    val horizontalPadding = 16.dp
    val verticalPadding = 12.dp
    val spacerWidth = 16.dp
    val avatarSize = 52.dp

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showDialog = true },
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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(spacerWidth))
        IconButton(onClick = { showDropdownMenu = true }) {
            val rotation by animateFloatAsState(
                targetValue = if (showDropdownMenu) 90f else 0f, label = "arrowRotation"
            )
            val iconColor by animateColorAsState(
                targetValue = if (showDropdownMenu) MaterialTheme.colorScheme.onBackground
                else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "iconColor"
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.rotate(rotation)
            )
            DropdownMenu(
                expanded = showDropdownMenu,
                onDismissRequest = { showDropdownMenu = false },
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.widthIn(min = 132.dp)
            ) {
                DropdownMenuItem(
                    text = { Text(text = "Delete") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.fill_trash),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.error,
                        leadingIconColor = MaterialTheme.colorScheme.error,
                    ),
                    onClick = {
                        showDropdownMenu = false
                        showConfirmDialog = true
                    }
                )
                DropdownMenuItem(
                    text = { Text(text = "More") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null,
                            modifier = Modifier.rotate(90f)
                        )
                    },
                    onClick = {
                        showDropdownMenu = false
                        showDialog = true
                    }
                )
            }
        }

        if (showConfirmDialog) {
            ConfirmDialog(
                title = "Delete friend with ${friend.name}",
                message = "Are you sure you want to unfriend?",
                confirmText = "Delete",
                cancelText = "Cancel",
                onDismissRequest = { showConfirmDialog = false },
                onConfirm = {
                    showConfirmDialog = false
                    onDelete()
                },
                onCancel = { showConfirmDialog = false },
            )
        }

        if (showDialog) {
            BasicAlertDialog(
                onDismissRequest = { showDialog = false },
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
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
                            .size(72.dp)
                            .clip(CircleShape),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = friend.name ?: "",
                        fontSize = 18.sp,
                        lineHeight = 18.sp,
                        fontFamily = Roboto,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    HorizontalDivider()
                    ListItem(
                        headlineContent = {
                            Text(
                                text = "See profile",
                                fontSize = 14.sp,
                                lineHeight = 14.sp,
                                fontFamily = Roboto,
                                fontWeight = FontWeight.Normal,
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                onClick = {
                                    showDialog = false
                                    onClick()
                                }
                            ),
                    )
                    HorizontalDivider()

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val textStyle = TextStyle(
                            fontSize = 14.sp, lineHeight = 14.sp,
                            fontFamily = Roboto, fontWeight = FontWeight.Medium
                        )

                        Button(
                            onClick = { showConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = "Unfriend",
                                style = textStyle,
                            )
                        }
                        Spacer(modifier = Modifier.width(32.dp))
                        Button(
                            onClick = {
                                showDialog = false
                                onMessage()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Message",
                                style = textStyle,
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.apcs.worknestapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.components.inputfield.CustomTextField
import com.apcs.worknestapp.ui.theme.Roboto

enum class ChatMode {
    TEXT_CHAT,
    SELECT_IMAGE,
    VOICE_CHAT,
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatInputSection(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    var currentChatMode by remember { mutableStateOf(ChatMode.TEXT_CHAT) }
    var currentText by remember(text) { mutableStateOf(text) }
    val interactionSource = remember { MutableInteractionSource() }
    var isCollapsed by remember { mutableStateOf(false) }

    val boxButtonPadding = PaddingValues(horizontal = 10.dp, vertical = 16.dp)

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when(interaction) {
                is PressInteraction.Press, is FocusInteraction.Focus -> {
                    isCollapsed = true
                }

                is FocusInteraction.Unfocus -> {
                    isCollapsed = false
                }

                else -> Unit
            }
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        if (isCollapsed) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clickable(onClick = { isCollapsed = false })
                    .padding(boxButtonPadding)
            ) {
                Icon(
                    painter = painterResource(R.drawable.symbol_angle_arrow),
                    contentDescription = "Expand",
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(270f)
                )
            }
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clickable(onClick = {})
                    .padding(boxButtonPadding)
            ) {
                Icon(
                    painter = painterResource(R.drawable.fill_camera),
                    contentDescription = "Camera",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clickable(onClick = {})
                    .padding(boxButtonPadding)
            ) {
                Icon(
                    painter = painterResource(R.drawable.fill_image),
                    contentDescription = "Image",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clickable(onClick = {})
                    .padding(boxButtonPadding)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        CustomTextField(
            value = currentText,
            onValueChange = {
                onTextChange(it)
                isCollapsed = it.isNotEmpty()
            },
            interactionSource = interactionSource,
            textStyle = TextStyle(
                fontSize = 15.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp,
                fontFamily = Roboto, fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            ),
            placeholder = {
                Text(
                    text = if (isCollapsed) "Type something..." else "Aaaa",
                    fontSize = 15.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp,
                    fontFamily = Roboto, fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentPadding = PaddingValues(vertical = 10.dp, horizontal = 12.dp),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke((0.8).dp, MaterialTheme.colorScheme.outlineVariant),
            maxLines = if (isCollapsed) 4 else 1,
            keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Sentences)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clickable(enabled = text.isNotBlank(), onClick = { onSend() })
                .padding(boxButtonPadding)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Post comment",
                tint = if (text.isNotBlank()) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

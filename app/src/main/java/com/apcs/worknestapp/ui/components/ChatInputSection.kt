package com.apcs.worknestapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.components.inputfield.CustomTextField
import com.apcs.worknestapp.ui.theme.Roboto

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatInputSection(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentText by remember(text) { mutableStateOf(text) }
    val interactionSource = remember { MutableInteractionSource() }
    var isCollapsed by remember { mutableStateOf(false) }
    val boxButtonPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp)

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
                    contentDescription = "Camera",
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
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        CustomTextField(
            value = currentText,
            onValueChange = {
                onTextChange(it)
                if (it.isEmpty()) isCollapsed = false
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
            border = BorderStroke(
                width = (0.8).dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            ),
            maxLines = if (isCollapsed) 1 else 4,
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.Sentences,
            )
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clickable(
                    enabled = text.isNotBlank(),
                    onClick = { onSend() }
                )
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

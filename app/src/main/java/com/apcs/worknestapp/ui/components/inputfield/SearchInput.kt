package com.apcs.worknestapp.ui.components.inputfield

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.theme.Roboto

@Composable
fun SearchInput(
    value: String,
    onValueChange: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource? = null,
) {
    val isFocused by interactionSource?.collectIsFocusedAsState()
        ?: remember { mutableStateOf(false) }

    val borderColor =
        if (isFocused) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outlineVariant

    val animationDuration = 200

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(it) },
            interactionSource = interactionSource,
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = Roboto,
                fontSize = 14.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .weight(1f)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(30),
                )
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(30),
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_magnifier),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    innerTextField()
                    if (value.isEmpty()) {
                        Text(
                            text = "Search",
                            fontSize = 14.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (!value.isEmpty()) {
                    Icon(
                        painter = painterResource(R.drawable.fill_close),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = { onValueChange("") })
                            .size(20.dp)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isFocused,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(animationDuration),
            ) + expandHorizontally(
                animationSpec = tween(animationDuration),
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(animationDuration),
            ) + shrinkHorizontally(
                animationSpec = tween(animationDuration),
            ),
        ) {
            TextButton(
                onClick = onCancel,
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 16.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = Roboto,
                )
            }
        }
    }
}

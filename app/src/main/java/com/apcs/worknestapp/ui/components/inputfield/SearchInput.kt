package com.apcs.worknestapp.ui.components.inputfield

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.components.RotatingIcon
import com.apcs.worknestapp.ui.theme.Roboto

@Composable
fun SearchInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSearching: Boolean = false,
    onCancel: () -> Unit,
    animationDuration: Int = 300,
    placeholder: (@Composable () -> Unit)? = null,
    textStyle: TextStyle = TextStyle(
        fontFamily = Roboto,
        fontSize = 14.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Normal,
        color = MaterialTheme.colorScheme.onSurface
    ),
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    border: BorderStroke? = null,
    shape: Shape = RoundedCornerShape(30),
    interactionSource: MutableInteractionSource? = null,
) {
    val isFocused by interactionSource?.collectIsFocusedAsState()
        ?: remember { mutableStateOf(false) }

    val cancelButtonHeight = 40.dp

    Row(
        modifier = modifier.height(cancelButtonHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(it) },
            interactionSource = interactionSource,
            singleLine = true,
            textStyle = textStyle,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .weight(1f)
                .let {
                    if (border != null) {
                        val borderColor = if (isFocused) border.brush
                        else SolidColor(MaterialTheme.colorScheme.outlineVariant)

                        return@let it.border(
                            width = border.width,
                            brush = borderColor,
                            shape = shape
                        )
                    }
                    return@let it
                }
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = shape,
                )
                .padding(contentPadding),
        ) { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSearching) {
                    RotatingIcon(
                        painter = painterResource(R.drawable.loading_icon_6),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.outline_magnifier),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    innerTextField()
                    if (value.isEmpty()) {
                        if (placeholder == null) {
                            Text(
                                text = "Search",
                                fontSize = 14.sp,
                                lineHeight = 14.sp,
                                fontFamily = Roboto,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else placeholder()
                    }
                }
                if (!value.isEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        painter = painterResource(R.drawable.fill_close),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = { onValueChange("") })
                            .size(16.dp)
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
                modifier = Modifier.height(cancelButtonHeight),
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 15.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = Roboto,
                )
            }
        }
    }
}

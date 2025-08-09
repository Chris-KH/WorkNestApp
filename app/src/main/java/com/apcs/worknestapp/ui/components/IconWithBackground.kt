package com.apcs.worknestapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

@Composable
fun IconWithBackground(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    contentPadding: PaddingValues = PaddingValues(8.dp),
) {
    Box(
        modifier = Modifier
            .background(color = containerColor, shape = CircleShape)
            .clip(CircleShape)
            .padding(contentPadding)

    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            tint = tint,
            modifier = modifier.align(alignment = Alignment.Center)
        )
    }
}

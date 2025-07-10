package com.apcs.worknestapp.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ErrorText(
    text: String,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    Text(
        text = text,
        color = MaterialTheme.colorScheme.error,
        fontSize = with(density) { 12.dp.toSp() },
        fontWeight = FontWeight.SemiBold,
        lineHeight = with(density) { 13.dp.toSp() },
        modifier = modifier.padding(start = 12.dp)
    )
}

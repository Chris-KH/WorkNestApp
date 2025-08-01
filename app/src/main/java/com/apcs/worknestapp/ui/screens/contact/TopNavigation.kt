package com.apcs.worknestapp.ui.screens.contact

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.ui.theme.Inter

@Composable
fun TopNavigation(
    visible: Boolean,
) {
    val animationDuration = 700

    var temp by remember { mutableStateOf(true) }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(durationMillis = animationDuration)
        ) + expandVertically(animationSpec = tween(durationMillis = animationDuration)),
        exit = fadeOut(
            animationSpec = tween(durationMillis = animationDuration)
        ) + shrinkVertically(animationSpec = tween(durationMillis = animationDuration))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .clickable(onClick = { temp = true })
                    .height(48.dp)
                    .weight(1f),
            ) {
                Text(
                    text = "Chat",
                    fontSize = 18.sp,
                    lineHeight = 18.sp,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(alignment = Alignment.Center)
                )
                HorizontalDivider(
                    modifier = Modifier.align(alignment = Alignment.BottomCenter),
                    thickness = 3.dp,
                    color = if (temp) MaterialTheme.colorScheme.primary
                    else Color.Unspecified,
                )
            }
            Box(
                modifier = Modifier
                    .clickable(onClick = { temp = false })
                    .height(48.dp)
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Contact",
                    fontSize = 18.sp,
                    lineHeight = 18.sp,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(alignment = Alignment.Center)
                )
                HorizontalDivider(
                    modifier = Modifier.align(alignment = Alignment.BottomCenter),
                    thickness = 3.dp,
                    color = if (!temp) MaterialTheme.colorScheme.primary
                    else Color.Unspecified,
                )
            }
        }
    }
}

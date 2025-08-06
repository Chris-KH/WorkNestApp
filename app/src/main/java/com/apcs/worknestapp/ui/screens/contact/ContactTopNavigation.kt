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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.ui.theme.NotoSerif
import com.apcs.worknestapp.ui.theme.Roboto

@Composable
fun ContactTopNavigation(
    currentSubScreen: ContactSubScreen,
    visible: Boolean,
    onNavigateToMessageScreen: () -> Unit,
    onNavigateToFriendScreen: () -> Unit,
) {
    val animationDuration = 700

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(durationMillis = animationDuration)
        ) + expandVertically(animationSpec = tween(durationMillis = animationDuration)),
        exit = fadeOut(
            animationSpec = tween(durationMillis = animationDuration)
        ) + shrinkVertically(animationSpec = tween(durationMillis = animationDuration)),
        label = "Contact screen top navigation"
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.surface),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            val textStyle = TextStyle(
                fontSize = 14.sp, lineHeight = 14.sp,
                fontFamily = Roboto, fontWeight = FontWeight.Medium,
            )
            val boxButtonModifier = Modifier
                .height(44.dp)
                .weight(1f)

            Box(
                modifier = boxButtonModifier.clickable(onClick = onNavigateToMessageScreen),
            ) {
                val isSelected = currentSubScreen == ContactSubScreen.MESSAGES
                Text(
                    text = "Messages",
                    style = textStyle,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(alignment = Alignment.Center)
                )
                HorizontalDivider(
                    modifier = Modifier.align(alignment = Alignment.BottomCenter),
                    thickness = 2.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant,
                )
            }
            Box(
                modifier = boxButtonModifier.clickable(onClick = onNavigateToFriendScreen),
            ) {
                val isSelected = currentSubScreen == ContactSubScreen.FRIENDS
                Text(
                    text = "Friends",
                    style = textStyle,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(alignment = Alignment.Center)
                )
                HorizontalDivider(
                    modifier = Modifier.align(alignment = Alignment.BottomCenter),
                    thickness = 2.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }
}

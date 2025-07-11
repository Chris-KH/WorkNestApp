package com.apcs.worknestapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.screens.Screen

@Composable
fun NavIcon(
    screen: Screen,
    currentScreen: Screen,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val selected = currentScreen == screen
    val icon = when (screen) {
        is Screen.Home -> if (selected) R.drawable.fill_home else R.drawable.outline_home
        is Screen.Profile -> if (selected) R.drawable.fill_profile else R.drawable.outline_profile
        else -> if (selected) R.drawable.fill_unknown else R.drawable.outline_unknown
    }

    val density = LocalDensity.current
    val fixedTextSize = with(density) { 13.dp.toSp() }

    Box(
        modifier = modifier
            .clickable { if (!selected) onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = screen.route,
                modifier = Modifier.size(24.dp),
                tint = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = screen.title,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = fixedTextSize,
                lineHeight = fixedTextSize,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

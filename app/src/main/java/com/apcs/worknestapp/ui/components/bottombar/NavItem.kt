package com.apcs.worknestapp.ui.components.bottombar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.screens.Screen
import com.apcs.worknestapp.ui.theme.Roboto

@Composable
fun RowScope.NavItem(
    screen: Screen,
    currentScreen: Screen,
    navController: NavHostController,
) {
    val selected = currentScreen == screen
    val icon = when(screen) {
        is Screen.Home -> if (selected) R.drawable.fill_home else R.drawable.outline_home
        is Screen.Profile -> if (selected) R.drawable.fill_profile else R.drawable.outline_profile
        is Screen.Contact -> if (selected) R.drawable.fill_contacts else R.drawable.outline_contacts
        is Screen.Notification -> if (selected) R.drawable.fill_bell else R.drawable.outline_bell
        is Screen.Note -> if (selected) R.drawable.fill_note else R.drawable.outline_note
        else -> if (selected) R.drawable.fill_unknown else R.drawable.outline_unknown
    }

    NavigationBarItem(
        selected = selected,
        onClick = {
            if (!selected) {
                navController.navigate(screen.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    restoreState = true
                    launchSingleTop = true
                }
            }
        },
        icon = {
            Icon(
                painter = painterResource(icon),
                contentDescription = screen.route,
                modifier = Modifier.size(24.dp),
            )
        },
        label = {
            Text(
                text = screen.title,
                fontSize = TextUnit(2.5f, TextUnitType.Em),
                lineHeight = TextUnit(1f, TextUnitType.Em),
                letterSpacing = 0.sp,
                fontFamily = Roboto,
                fontWeight = FontWeight.Medium
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
    )
}

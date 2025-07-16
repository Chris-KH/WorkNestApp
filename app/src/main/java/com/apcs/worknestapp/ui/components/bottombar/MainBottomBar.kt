package com.apcs.worknestapp.ui.components.bottombar

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.apcs.worknestapp.ui.components.NavItem
import com.apcs.worknestapp.ui.screens.Screen

@Composable
fun MainBottomBar(currentScreen: Screen, navController: NavHostController) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = (0.5).dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        NavigationBar(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ) {
            NavItem(
                screen = Screen.Home,
                currentScreen = currentScreen,
            ) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = false }
                    launchSingleTop = true
                }
            }
            NavItem(
                screen = Screen.NotificationScreen,
                currentScreen = currentScreen,
            ) {
                navController.navigate(Screen.NotificationScreen.route) {
                    popUpTo(Screen.NotificationScreen.route) { inclusive = false }
                    launchSingleTop = true
                }
            }
            NavItem(
                screen = Screen.Profile,
                currentScreen = currentScreen,
            ) {
                navController.navigate(Screen.Profile.route) {
                    popUpTo(Screen.Profile.route) { inclusive = false }
                    launchSingleTop = true
                }
            }
        }
    }
}

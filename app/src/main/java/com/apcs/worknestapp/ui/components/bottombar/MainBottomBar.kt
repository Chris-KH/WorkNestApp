package com.apcs.worknestapp.ui.components.bottombar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.apcs.worknestapp.ui.screens.Screen

@Composable
fun MainBottomBar(currentScreen: Screen, navController: NavHostController) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = (1).dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        NavigationBar(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            NavItem(
                screen = Screen.Home,
                currentScreen = currentScreen,
                navController = navController,
            )
            NavItem(
                screen = Screen.Note,
                currentScreen = currentScreen,
                navController = navController,
            )
            NavItem(
                screen = Screen.Contact,
                currentScreen = currentScreen,
                navController = navController,
            )
            NavItem(
                screen = Screen.Notification,
                currentScreen = currentScreen,
                navController = navController,
            )
            NavItem(
                screen = Screen.MyProfile,
                currentScreen = currentScreen,
                navController = navController,
            )
        }
    }
}

package com.apcs.worknestapp.ui.components.bottombar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.apcs.worknestapp.ui.components.NavIcon
import com.apcs.worknestapp.ui.screens.Screen

@Composable
fun MainBottomBar(currentScreen: Screen, navController: NavHostController) {
    BottomAppBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 0.dp)
        ) {
            NavIcon(
                screen = Screen.Home,
                currentScreen = currentScreen,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = false }
                    launchSingleTop = true
                }
            }
            NavIcon(
                screen = Screen.Profile,
                currentScreen = currentScreen,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                navController.navigate(Screen.Profile.route) {
                    popUpTo(Screen.Profile.route) { inclusive = false }
                    launchSingleTop = true
                }
            }
        }
    }
}

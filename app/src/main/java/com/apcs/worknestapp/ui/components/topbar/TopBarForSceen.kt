package com.apcs.worknestapp.ui.components.topbar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.apcs.worknestapp.ui.screens.Screen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarForScreen(
    screen: Screen?,
    navController: NavHostController,
) {
    when (screen) {
        Screen.Home -> {
            TopBarHomeScreen(navController = navController)
        }

        Screen.Profile -> {
            TopBarProfileScreen(navController = navController)
        }

        Screen.Notification -> {
            TopBarNotificationScreen(navController = navController)
        }


        else -> {}
    }
}

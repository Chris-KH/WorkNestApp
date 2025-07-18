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
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    when (screen) {
        Screen.Home -> {
            TopBarHomeScreen(navController = navController, scrollBehavior = scrollBehavior)
        }

        Screen.Profile -> {
            TopBarProfileScreen(navController = navController, scrollBehavior = scrollBehavior)
        }

        Screen.EditProfile,
        Screen.Setting,
            -> {
            ExitOnlyTopBar(navController = navController, screen = screen)
        }

        Screen.NotificationScreen -> {
            TopBarNotificationScreen(navController = navController, scrollBehavior = scrollBehavior)
        }


        else -> {}
    }
}

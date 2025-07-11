package com.apcs.worknestapp.ui.components.topbar

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.apcs.worknestapp.ui.screens.Screen


@Composable
fun TopBarForScreen(screen: Screen?, navController: NavHostController) {
    when (screen) {
        Screen.Home -> {
            TopBarHomeScreen(navController = navController)
        }

        Screen.Profile -> {
            TopBarProfileScreen(navController = navController)
        }

        else -> {}
    }
}

package com.apcs.worknestapp.ui.components.bottombar

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.apcs.worknestapp.ui.screens.Screen

@Composable
fun BottomBarForScreen(screen: Screen?, navController: NavHostController) {
    when (screen) {
        Screen.Home,
        Screen.Profile,
        Screen.Notification,
        Screen.Note,
            -> {
            MainBottomBar(currentScreen = screen, navController = navController)
        }

        else -> {}
    }
}

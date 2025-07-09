package com.apcs.worknestapp

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.apcs.worknestapp.ui.screens.Screen
import com.apcs.worknestapp.ui.screens.home.HomeScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Home.route,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController, modifier = modifier)
        }
    }
}

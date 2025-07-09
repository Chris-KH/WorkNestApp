package com.apcs.worknestapp

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.apcs.worknestapp.state.rememberNetworkState
import com.apcs.worknestapp.ui.components.CustomSnackBar
import com.apcs.worknestapp.ui.screens.Screen

@Composable
fun MainLayout(startDestination: String) {
    val isNetworkConnected by rememberNetworkState()
    val navController: NavHostController = rememberNavController()
    val snackbarHost = remember { SnackbarHostState() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentScreen = Screen.fromRoute(currentRoute)

    LaunchedEffect(isNetworkConnected) {
        if (!isNetworkConnected) {
            snackbarHost.showSnackbar(
                message = "No internet connection",
                withDismissAction = true,
                duration = SnackbarDuration.Indefinite,
            )
        }
    }

    Scaffold(
        topBar = {
            //TODO
        },
        bottomBar = {
            //TODO
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHost) { data ->
                CustomSnackBar(data = data)
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            snackbarHost = snackbarHost,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

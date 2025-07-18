package com.apcs.worknestapp

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.apcs.worknestapp.state.rememberNetworkState
import com.apcs.worknestapp.ui.components.CustomSnackBar
import com.apcs.worknestapp.ui.components.bottombar.BottomBarForScreen
import com.apcs.worknestapp.ui.components.topbar.TopBarForScreen
import com.apcs.worknestapp.ui.screens.Screen
import com.apcs.worknestapp.ui.screens.editprofile.EditProfileScreen
import com.apcs.worknestapp.ui.screens.home.HomeScreen
import com.apcs.worknestapp.ui.screens.login.LoginScreen
import com.apcs.worknestapp.ui.screens.note.NoteScreen
import com.apcs.worknestapp.ui.screens.notification.NotificationScreen
import com.apcs.worknestapp.ui.screens.profile.ProfileScreen
import com.apcs.worknestapp.ui.screens.setting.SettingScreen
import com.apcs.worknestapp.ui.screens.signup.SignUpScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(startDestination: String) {
    val isNetworkConnected by rememberNetworkState()
    val navController: NavHostController = rememberNavController()
    val snackbarHost = remember { SnackbarHostState() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentScreen = Screen.fromRoute(currentRoute)

    val scrollBehavior = when (currentScreen) {
        else -> TopAppBarDefaults.pinnedScrollBehavior()
    }

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
            TopBarForScreen(
                screen = currentScreen,
                navController = navController,
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = { BottomBarForScreen(screen = currentScreen, navController = navController) },
        snackbarHost = { SnackbarHost(snackbarHost) { CustomSnackBar(data = it) } },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier,
        ) {
            composable(route = Screen.Home.route) {
                HomeScreen(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            composable(route = Screen.Note.route) {
                NoteScreen(
                    navController = navController,
                    snackbarHost = snackbarHost,
                    modifier = Modifier,
                )
            }

            composable(route = Screen.Notification.route) {
                NotificationScreen(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            composable(route = Screen.Profile.route) {
                ProfileScreen(
                    navController = navController,
                    snackbarHost = snackbarHost,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            composable(
                route = Screen.EditProfile.route,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(700)
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(700)
                    )
                },
            ) {
                EditProfileScreen(
                    navController = navController,
                    snackbarHost = snackbarHost,
                    modifier = Modifier,
                )
            }

            composable(
                route = Screen.Setting.route,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(700)
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(700)
                    )
                },
            ) {
                SettingScreen(
                    navController = navController,
                    snackbarHost = snackbarHost,
                    modifier = Modifier,
                )
            }

            composable(route = Screen.Login.route) {
                LoginScreen(
                    navController = navController,
                    snackbarHost = snackbarHost,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            composable(route = Screen.SignUp.route) {
                SignUpScreen(
                    navController = navController,
                    snackbarHost = snackbarHost,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.apcs.worknestapp.state.rememberNetworkState
import com.apcs.worknestapp.ui.components.CustomSnackBar
import com.apcs.worknestapp.ui.components.FallbackScreen
import com.apcs.worknestapp.ui.components.bottombar.BottomBarForScreen
import com.apcs.worknestapp.ui.screens.Screen
import com.apcs.worknestapp.ui.screens.edit_profile.EditProfileScreen
import com.apcs.worknestapp.ui.screens.edit_profile_detail.EditProfileDetailScreen
import com.apcs.worknestapp.ui.screens.edit_profile.EditProfileField
import com.apcs.worknestapp.ui.screens.home.HomeScreen
import com.apcs.worknestapp.ui.screens.login.LoginScreen
import com.apcs.worknestapp.ui.screens.note.NoteScreen
import com.apcs.worknestapp.ui.screens.notification.NotificationScreen
import com.apcs.worknestapp.ui.screens.profile.ProfileScreen
import com.apcs.worknestapp.ui.screens.setting.SettingField
import com.apcs.worknestapp.ui.screens.setting.SettingScreen
import com.apcs.worknestapp.ui.screens.setting_detail.SettingDetailScreen
import com.apcs.worknestapp.ui.screens.signup.SignUpScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(startDestination: String) {
    val isNetworkConnected by rememberNetworkState()
    val navController: NavHostController = rememberNavController()
    val snackbarHost = remember { SnackbarHostState() }

    val transitionDuration = 500

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
        snackbarHost = { SnackbarHost(snackbarHost) { CustomSnackBar(data = it) } },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = { fadeIn(animationSpec = tween(transitionDuration)) },
            exitTransition = { fadeOut(animationSpec = tween(transitionDuration)) },
            modifier = Modifier,
        ) {
            composable(route = Screen.Home.route) {
                HomeScreen(
                    navController = navController,
                    modifier = Modifier,
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
                    snackbarHost = snackbarHost,
                    modifier = Modifier,
                )
            }

            composable(
                route = Screen.Profile.route,
                popEnterTransition = {
                    fadeIn(animationSpec = tween(transitionDuration))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(transitionDuration)
                    )
                },
            ) {
                ProfileScreen(
                    navController = navController,
                    snackbarHost = snackbarHost,
                    modifier = Modifier,
                )
            }

            composable(
                route = Screen.EditProfile.route,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(transitionDuration)
                    )
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(transitionDuration))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(transitionDuration)
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
                route = Screen.EditProfileDetail.route,
                arguments = listOf(navArgument("field") {
                    type = NavType.StringType
                    nullable = false
                }),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(transitionDuration)
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(transitionDuration)
                    )
                },
            ) { backStackEntry ->
                val rawField = backStackEntry.arguments?.getString("field")
                val field = EditProfileField.fromRoute(rawField)

                if (field != null) {
                    EditProfileDetailScreen(
                        field = field,
                        navController = navController,
                        snackbarHost = snackbarHost,
                        modifier = Modifier,
                    )
                } else {
                    FallbackScreen(
                        message = "Cannot open this edit screen for some reason, try again later.",
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }

            composable(
                route = Screen.Setting.route,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(transitionDuration)
                    )
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(transitionDuration))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(transitionDuration)
                    )
                },
            ) {
                SettingScreen(
                    navController = navController,
                    snackbarHost = snackbarHost,
                    modifier = Modifier,
                )
            }

            composable(
                route = Screen.SettingDetail.route,
                arguments = listOf(navArgument("field") {
                    type = NavType.StringType
                    nullable = false
                }),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(transitionDuration)
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(transitionDuration)
                    )
                },
            ) { backStackEntry ->
                val rawField = backStackEntry.arguments?.getString("field")
                val field = SettingField.fromRoute(rawField)

                if (field != null) {
                    SettingDetailScreen(
                        field = field,
                        navController = navController,
                        snackbarHost = snackbarHost,
                        modifier = Modifier,
                    )
                } else {
                    FallbackScreen(
                        message = "Cannot open this setting screen for some reason, try again later.",
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
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

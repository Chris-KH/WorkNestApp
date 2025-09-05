package com.apcs.worknestapp

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.apcs.worknestapp.state.rememberNetworkState
import com.apcs.worknestapp.ui.components.CustomSnackBar
import com.apcs.worknestapp.ui.components.FallbackScreen
import com.apcs.worknestapp.ui.screens.Screen
import com.apcs.worknestapp.ui.screens.add_contact.AddContractScreen
import com.apcs.worknestapp.ui.screens.board.BoardNoteDetailScreen
import com.apcs.worknestapp.ui.screens.board.BoardScreen
import com.apcs.worknestapp.ui.screens.chat.ChatScreen
import com.apcs.worknestapp.ui.screens.contact.ContactScreen
import com.apcs.worknestapp.ui.screens.edit_profile.EditProfileField
import com.apcs.worknestapp.ui.screens.edit_profile.EditProfileScreen
import com.apcs.worknestapp.ui.screens.edit_profile_detail.EditProfileDetailScreen
import com.apcs.worknestapp.ui.screens.home.HomeScreen
import com.apcs.worknestapp.ui.screens.login.LoginScreen
import com.apcs.worknestapp.ui.screens.my_profile.MyProfileScreen
import com.apcs.worknestapp.ui.screens.note.NoteScreen
import com.apcs.worknestapp.ui.screens.note_detail.NoteDetailScreen
import com.apcs.worknestapp.ui.screens.notification.NotificationScreen
import com.apcs.worknestapp.ui.screens.setting.SettingField
import com.apcs.worknestapp.ui.screens.setting.SettingScreen
import com.apcs.worknestapp.ui.screens.setting_detail.SettingDetailScreen
import com.apcs.worknestapp.ui.screens.setting_detail.setting_account.SettingAccountDetailScreen
import com.apcs.worknestapp.ui.screens.setting_detail.setting_account.SettingAccountField
import com.apcs.worknestapp.ui.screens.signup.SignUpScreen
import com.apcs.worknestapp.ui.screens.user_profile.UserProfileScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(startDestination: String) {
    val isNetworkConnected by rememberNetworkState()
    val navController: NavHostController = rememberNavController()
    val snackbarHost = remember { SnackbarHostState() }

    val transitionDuration = 500

    LaunchedEffect(Unit) {
        navController.enableOnBackPressed(true)
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
                    snackbarHost = snackbarHost,
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

            composable(route = Screen.Contact.route) {
                ContactScreen(
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

            composable(route = Screen.MyProfile.route) {
                MyProfileScreen(
                    navController = navController,
                    snackbarHost = snackbarHost,
                    modifier = Modifier,
                )
            }

            composable(
                route = Screen.UserProfile.route,
                arguments = listOf(navArgument("userId") {
                    type = NavType.StringType
                }),
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
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")
                if (userId != null) {
                    UserProfileScreen(
                        userId = userId,
                        navController = navController,
                        snackbarHost = snackbarHost,
                        modifier = Modifier,
                    )
                } else {
                    FallbackScreen(
                        message = "User profile not found.",
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }

            composable(
                route = Screen.AddContact.route,
                enterTransition = {
                    slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(transitionDuration)
                    )
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(transitionDuration))
                },
                popExitTransition = {
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(transitionDuration)
                    )
                }
            ) {
                AddContractScreen(
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
                popEnterTransition = {
                    fadeIn(animationSpec = tween(transitionDuration))
                },
                popExitTransition = {
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

            composable(
                route = Screen.SettingAccount.route,
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
                popEnterTransition = {
                    fadeIn(animationSpec = tween(transitionDuration))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(transitionDuration)
                    )
                },
            ) { backStackEntry ->
                val rawField = backStackEntry.arguments?.getString("field")
                val field = SettingAccountField.fromRoute(rawField)

                if (field != null) {
                    SettingAccountDetailScreen(
                        field = field,
                        navController = navController,
                        snackbarHost = snackbarHost,
                        modifier = Modifier,
                    )
                } else {
                    FallbackScreen(
                        message = "Cannot open account setting screen for some reason, try again later.",
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

            composable(
                route = Screen.NoteDetail.route, // Make sure Screen.NoteDetail.route is "note_detail/{noteId}"
                arguments = listOf(navArgument("noteId") {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getString("noteId")
                if (noteId.isNullOrBlank()) {
                    FallbackScreen(
                        message = "Cannot open this note. Note ID is missing.",
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                } else {
                    NoteDetailScreen(
                        navController = navController,
                        snackbarHost = snackbarHost,
                        noteId = noteId
                    )
                }
            }
            composable(
                route = Screen.BoardNoteDetail.route + "/{boardId}/{noteListId}/{noteId}",
                arguments = listOf(
                    navArgument("boardId") { type = NavType.StringType },
                    navArgument("noteListId") { type = NavType.StringType },
                    navArgument("noteId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val boardId = backStackEntry.arguments?.getString("boardId")
                val notelistId = backStackEntry.arguments?.getString("noteListId")
                val noteId = backStackEntry.arguments?.getString("noteId")

                if (boardId.isNullOrBlank() || notelistId.isNullOrBlank() || noteId.isNullOrBlank()) {
                    FallbackScreen(
                        message = "Cannot open this note. One or more IDs are missing.",
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                } else {
                    BoardNoteDetailScreen(
                        boardId = boardId,
                        noteListId = notelistId,
                        noteId = noteId,
                        snackbarHost = snackbarHost,
                        navController = navController,
                        modifier = Modifier,
                    )
                }
            }
            composable(
                route = "board/{boardId}",
                arguments = listOf(navArgument("boardId") { type = NavType.StringType })
            ) { backStackEntry ->
                val boardId = backStackEntry.arguments?.getString("boardId")
                if (boardId != null) {
                    BoardScreen(
                        navController = navController,
                        snackbarHost = snackbarHost,
                        modifier = Modifier,
                        boardId = boardId
                    )
                } else {
                    FallbackScreen(
                        message = "Error: Board ID missing.",
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }

            composable(
                route = Screen.Chat.route,
                arguments = listOf(navArgument("conservationId") {
                    type = NavType.StringType
                }),
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
            ) { backStackEntry ->
                val conservationId = backStackEntry.arguments?.getString("conservationId")
                if (conservationId.isNullOrBlank()) {
                    FallbackScreen(
                        message = "Cannot found this chat.",
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                } else {
                    ChatScreen(
                        conservationId = conservationId,
                        navController = navController,
                        snackbarHost = snackbarHost,
                    )
                }
            }
        }
    }
}

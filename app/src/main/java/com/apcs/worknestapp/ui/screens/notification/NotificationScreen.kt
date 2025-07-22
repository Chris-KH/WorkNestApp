package com.apcs.worknestapp.ui.screens.notification

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.apcs.worknestapp.ui.components.bottombar.BottomBarForScreen
import com.apcs.worknestapp.ui.components.topbar.ExitOnlyTopBar
import com.apcs.worknestapp.ui.components.topbar.TopBarNoteScreen
import com.apcs.worknestapp.ui.components.topbar.TopBarNotificationScreen
import com.apcs.worknestapp.ui.screens.Screen

@Composable
fun NotificationScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopBarNotificationScreen(
                navController = navController,
            )
        },
        bottomBar = {
            BottomBarForScreen(
                screen = Screen.Notification,
                navController = navController,
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding),
        ) {
            Text(text = "NotificationScreen")
        }
    }
}

package com.apcs.worknestapp.ui.screens.editprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.apcs.worknestapp.ui.components.topbar.ExitOnlyTopBar
import com.apcs.worknestapp.ui.screens.Screen

@Composable
fun EditProfileScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            ExitOnlyTopBar(
                screen = Screen.EditProfile,
                navController = navController,
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
        ) {
            Text(text = "EditScreen")
        }
    }

}

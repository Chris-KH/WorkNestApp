package com.apcs.worknestapp.ui.screens.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.apcs.worknestapp.LocalAuthViewModel
import com.apcs.worknestapp.ui.screens.Screen
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val authViewModel = LocalAuthViewModel.current
    val profile = authViewModel.profile.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier,
    ) {
        Button(
            onClick = {
                coroutineScope.launch {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
            }
        ) {
            Text(text = "Log out", modifier = Modifier)
        }
    }
}

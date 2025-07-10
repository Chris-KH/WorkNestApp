package com.apcs.worknestapp.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.apcs.worknestapp.LocalAuthViewModel
import com.apcs.worknestapp.ui.screens.Screen
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val authViewModel = LocalAuthViewModel.current
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

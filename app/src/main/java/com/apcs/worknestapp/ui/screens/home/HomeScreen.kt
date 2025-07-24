package com.apcs.worknestapp.ui.screens.home

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavHostController
import com.apcs.worknestapp.ui.components.bottombar.MainBottomBar
import com.apcs.worknestapp.ui.components.topbar.TopBarHomeScreen
import com.apcs.worknestapp.ui.screens.Screen

@Composable
fun HomeScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    var value by remember { mutableStateOf("") }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Scaffold(
        topBar = {
            TopBarHomeScreen(navController = navController)
        },
        bottomBar = {
            MainBottomBar(
                currentScreen = Screen.Home,
                navController = navController,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .fillMaxWidth(),
        ) {
            QuickAddNoteInput(
                value = value,
                onValueChange = { value = it },
                onCancel = { focusManager.clearFocus() },
                onAdd = {},
                isFocused = isFocused,
                interactionSource = interactionSource,
            )
        }
    }

}

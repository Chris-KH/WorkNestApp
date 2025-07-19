package com.apcs.worknestapp.ui.screens.setting

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.apcs.worknestapp.ui.components.inputfield.SearchInput
import com.apcs.worknestapp.ui.components.topbar.ExitOnlyTopBar
import com.apcs.worknestapp.ui.screens.Screen

@Composable
fun SettingScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    var searchValue by remember { mutableStateOf("") }
    val searchInteractionSource = remember { MutableInteractionSource() }

    val horizontalPadding = 12.dp

    Scaffold(
        topBar = {
            ExitOnlyTopBar(
                navController = navController,
                screen = Screen.Setting
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            SearchInput(
                value = searchValue,
                onValueChange = { searchValue = it },
                onCancel = {
                    focusManager.clearFocus()
                    searchValue = ""
                },
                interactionSource = searchInteractionSource,
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
                    .padding(top = 8.dp),
            )
        }
    }
}

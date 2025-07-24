package com.apcs.worknestapp.ui.screens.home

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.apcs.worknestapp.ui.components.bottombar.MainBottomBar
import com.apcs.worknestapp.ui.components.inputfield.SearchInput
import com.apcs.worknestapp.ui.components.topbar.TopBarHomeScreen
import com.apcs.worknestapp.ui.screens.Screen
import com.apcs.worknestapp.ui.theme.Roboto

@Composable
fun HomeScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopBarHomeScreen(
                actions = {
                    var menuExpanded by remember { mutableStateOf(false) }

                    IconButton(
                        onClick = { menuExpanded = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(28.dp)
                                .rotate(-90f)
                        )
                    }
                    DropdownMenuActions(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        onCreateBoard = { menuExpanded = false },
                        onCreateCard = { menuExpanded = false },
                    )
                }
            )
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
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .fillMaxWidth(),
        ) {
            var searchValue by remember { mutableStateOf("") }
            var noteValue by remember { mutableStateOf("") }

            val horizontalPadding = 16.dp

            SearchInput(
                value = searchValue,
                onValueChange = { searchValue = it },
                onCancel = { focusManager.clearFocus() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = 16.dp),
                interactionSource = remember { MutableInteractionSource() }
            )

            QuickAddNoteInput(
                value = noteValue,
                onValueChange = { noteValue = it },
                onCancel = { focusManager.clearFocus() },
                onAdd = {},
                interactionSource = remember { MutableInteractionSource() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = 12.dp)
            )

            Text(
                text = "Your workspaces".uppercase(),
                fontSize = 16.sp,
                lineHeight = 16.sp,
                fontFamily = Roboto,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = 16.dp)
            )
        }
    }
}

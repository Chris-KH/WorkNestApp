package com.apcs.worknestapp.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.components.bottombar.MainBottomBar
import com.apcs.worknestapp.ui.components.topbar.CustomTopBar
import com.apcs.worknestapp.ui.components.topbar.MainTopBar
import com.apcs.worknestapp.ui.screens.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    var currentSubScreen by rememberSaveable { mutableStateOf(HomeSubScreenState.MAIN) }
    var showModalBottom by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            when(currentSubScreen) {
                HomeSubScreenState.MAIN ->
                    MainTopBar(
                        title = "WorkNest",
                        actions = {
                            var menuExpanded by remember { mutableStateOf(false) }

                            IconButton(
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    disabledContentColor = Color.Unspecified,
                                ),
                                onClick = { menuExpanded = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp)
                                )
                                HomeDropdownActions(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false },
                                    onCreateBoard = { menuExpanded = false },
                                    onCreateCard = { menuExpanded = false },
                                )
                            }
                        }
                    )

                HomeSubScreenState.WORKSPACE ->
                    CustomTopBar(
                        field = "Không gian của tôi",
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            scrolledContainerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        navigationIcon = {
                            IconButton(
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    disabledContentColor = Color.Unspecified,
                                ),
                                onClick = { currentSubScreen = HomeSubScreenState.MAIN }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.symbol_angle_arrow),
                                    contentDescription = "back",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .rotate(90f)
                                )
                            }
                        },
                        actions = {
                            var menuExpanded by remember { mutableStateOf(false) }

                            IconButton(
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    disabledContentColor = Color.Unspecified,
                                ),
                                onClick = { menuExpanded = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp)
                                )
                                HomeDropdownActions(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false },
                                    onCreateBoard = { menuExpanded = false },
                                    onCreateCard = { menuExpanded = false },
                                )
                            }
                            IconButton(
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    disabledContentColor = Color.Unspecified,
                                ),
                                onClick = { showModalBottom = true },
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.symbol_three_dot),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .rotate(-90f)
                                )
                            }
                        },
                    )
            }
        },
        bottomBar = {
            MainBottomBar(
                currentScreen = Screen.Home,
                navController = navController
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentSubScreen,
            transitionSpec = {
                if (initialState == HomeSubScreenState.MAIN && targetState == HomeSubScreenState.WORKSPACE) {
                    (slideInHorizontally { it } + fadeIn())
                        .togetherWith(slideOutHorizontally { -it } + fadeOut())
                } else {
                    (slideInHorizontally { -it } + fadeIn())
                        .togetherWith(slideOutHorizontally { it } + fadeOut())
                }.using(SizeTransform(clip = false))
            },
            contentAlignment = Alignment.TopStart,
            label = "HomeContentSwitch"
        ) {
            when(it) {
                HomeSubScreenState.MAIN -> HomeMainScreen(
                    modifier = Modifier.padding(innerPadding),
                    onNavigateToWorkspace = { currentSubScreen = HomeSubScreenState.WORKSPACE }
                )

                HomeSubScreenState.WORKSPACE -> HomeWorkspaceScreen(
                    modifier = Modifier.padding(innerPadding),
                    showModalBottom = showModalBottom,
                    onHideModal = { showModalBottom = false }
                )
            }
        }
    }
}

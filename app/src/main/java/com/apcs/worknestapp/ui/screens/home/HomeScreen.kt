package com.apcs.worknestapp.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.board.BoardViewModel
import com.apcs.worknestapp.ui.components.bottombar.MainBottomBar
import com.apcs.worknestapp.ui.components.topbar.CustomTopBar
import com.apcs.worknestapp.ui.components.topbar.MainTopBar
import com.apcs.worknestapp.ui.screens.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
    boardViewModel: BoardViewModel = hiltViewModel(),
) {
    val isFirstLoad = rememberSaveable { mutableStateOf(true) }
    var currentSubScreen by rememberSaveable { mutableStateOf(HomeSubScreenState.MAIN) }
    var showModalBottom by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            when(currentSubScreen) {
                HomeSubScreenState.MAIN ->
                    MainTopBar(
                        title = "WorkNest",
                        actions = {
                            var menuExpanded by remember { mutableStateOf(false) }

                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp)
                                )
                                HomeDropdownActions(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false },
                                    onCreateBoard = {
                                        menuExpanded = false
                                        coroutineScope.launch {
                                            val isSuccess = boardViewModel.createBoard()
                                            if (!isSuccess) {
                                                snackbarHost.showSnackbar(
                                                    message = "Create board failed.",
                                                    withDismissAction = true,
                                                )
                                            }
                                        }
                                    },
                                    onCreateCard = { menuExpanded = false },
                                )
                            }
                        }
                    )

                HomeSubScreenState.WORKSPACE ->
                    CustomTopBar(
                        field = "My Workspace",
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            scrolledContainerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            navigationIconContentColor = MaterialTheme.colorScheme.primary,
                            actionIconContentColor = MaterialTheme.colorScheme.primary
                        ),
                        navigationIcon = {
                            IconButton(onClick = { currentSubScreen = HomeSubScreenState.MAIN }) {
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

                            IconButton(onClick = { menuExpanded = true }) {
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
                            IconButton(onClick = { showModalBottom = true }) {
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
            label = "HomeContentSwitch",
            modifier = Modifier,
        ) {
            when(it) {
                HomeSubScreenState.MAIN -> HomeMainScreen(
                    isFirstLoad = isFirstLoad.value,
                    onFirstLoadDone = { isFirstLoad.value = false },
                    navController = navController,
                    snackbarHost = snackbarHost,
                    modifier = Modifier
                        .padding(
                            top = innerPadding.calculateTopPadding(),
                            start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                            end = innerPadding.calculateEndPadding(LayoutDirection.Ltr)
                        )
                        .padding(
                            bottom = if (WindowInsets.isImeVisible) 0.dp
                            else innerPadding.calculateBottomPadding()
                        ),
                    onNavigateToWorkspace = { currentSubScreen = HomeSubScreenState.WORKSPACE },
                )

                HomeSubScreenState.WORKSPACE -> HomeWorkspaceScreen(
                    isFirstLoad = isFirstLoad.value,
                    onFirstLoadDone = { isFirstLoad.value = false },
                    navController = navController,
                    snackbarHost = snackbarHost,
                    modifier = Modifier
                        .padding(
                            top = innerPadding.calculateTopPadding(),
                            start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                            end = innerPadding.calculateEndPadding(LayoutDirection.Ltr)
                        )
                        .padding(
                            bottom = if (WindowInsets.isImeVisible) 0.dp
                            else innerPadding.calculateBottomPadding()
                        ),
                    showModalBottom = showModalBottom,
                    onHideModal = { showModalBottom = false }
                )
            }
        }
    }
}

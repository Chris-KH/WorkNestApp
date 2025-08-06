package com.apcs.worknestapp.ui.screens.contact

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.components.bottombar.MainBottomBar
import com.apcs.worknestapp.ui.components.topbar.MainTopBar
import com.apcs.worknestapp.ui.screens.Screen

enum class ContactSubScreen {
    MESSAGES,
    FRIENDS,
}

@Composable
fun ContactScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            MainTopBar(
                title = Screen.Contact.title,
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(R.drawable.outline_add_user),
                            contentDescription = "Add more contact",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
            )
        },
        bottomBar = {
            MainBottomBar(
                currentScreen = Screen.Contact,
                navController = navController,
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        var currentSubScreen by rememberSaveable { mutableStateOf(ContactSubScreen.MESSAGES) }
        var topNavigationVisible by rememberSaveable { mutableStateOf(true) }

        val listStateMessageScreen = rememberLazyListState()
        var previousIndexMessageScreen by rememberSaveable { mutableIntStateOf(0) }
        val listStateFriendScreen = rememberLazyListState()
        var previousIndexFriendScreen by rememberSaveable { mutableIntStateOf(0) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .animateContentSize()
                .padding(innerPadding),
        ) {
            ContactTopNavigation(
                currentSubScreen = currentSubScreen,
                visible = topNavigationVisible,
                onNavigateToMessageScreen = {
                    if (currentSubScreen != ContactSubScreen.MESSAGES)
                        currentSubScreen = ContactSubScreen.MESSAGES
                },
                onNavigateToFriendScreen = {
                    if (currentSubScreen != ContactSubScreen.FRIENDS)
                        currentSubScreen = ContactSubScreen.FRIENDS
                },
            )

            AnimatedContent(
                targetState = currentSubScreen,
                label = "Contact subscreen"
            ) {
                if (it == ContactSubScreen.MESSAGES) {
                    LaunchedEffect(listStateMessageScreen) {
                        snapshotFlow { listStateMessageScreen.firstVisibleItemIndex }
                            .collect { currentIndex ->
                                if (currentIndex == previousIndexMessageScreen) return@collect
                                if (currentIndex + 2 < previousIndexMessageScreen || currentIndex == 0) {
                                    topNavigationVisible = true
                                    previousIndexMessageScreen = currentIndex
                                } else if (currentIndex > previousIndexMessageScreen) {
                                    previousIndexMessageScreen = currentIndex
                                    topNavigationVisible = false
                                }
                            }
                    }

                    LazyColumn(
                        state = listStateMessageScreen,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(50) { item ->
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .padding(8.dp)
                                    .background(Color.Gray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Item $item", color = Color.White)
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                } else {
                    LaunchedEffect(listStateFriendScreen) {
                        snapshotFlow { listStateFriendScreen.firstVisibleItemIndex }
                            .collect { currentIndex ->
                                if (currentIndex == previousIndexFriendScreen) return@collect
                                if (currentIndex + 2 < previousIndexFriendScreen || currentIndex == 0) {
                                    topNavigationVisible = true
                                    previousIndexFriendScreen = currentIndex
                                } else if (currentIndex > previousIndexFriendScreen) {
                                    previousIndexFriendScreen = currentIndex
                                    topNavigationVisible = false
                                }
                            }
                    }

                    LazyColumn(
                        state = listStateFriendScreen,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(50) { item ->
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .padding(8.dp)
                                    .background(Color.Gray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Item ${item * 10}", color = Color.White)
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(60.dp))
                        }
                    }
                }
            }
        }
    }
}

package com.apcs.worknestapp.ui.screens.contact

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.components.bottombar.MainBottomBar
import com.apcs.worknestapp.ui.components.topbar.MainTopBar
import com.apcs.worknestapp.ui.screens.Screen

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
        var currentSubScreen by rememberSaveable { mutableStateOf(ContactSubScreenState.MESSAGES) }
        var topNavigationVisible by rememberSaveable { mutableStateOf(true) }

        val listStateMessageScreen = rememberLazyListState()
        val previousIndexMessageScreen = rememberSaveable { mutableIntStateOf(0) }
        val listStateFriendScreen = rememberLazyListState()
        val previousIndexFriendScreen = rememberSaveable { mutableIntStateOf(0) }

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
                    if (currentSubScreen != ContactSubScreenState.MESSAGES)
                        currentSubScreen = ContactSubScreenState.MESSAGES
                },
                onNavigateToFriendScreen = {
                    if (currentSubScreen != ContactSubScreenState.FRIENDS)
                        currentSubScreen = ContactSubScreenState.FRIENDS
                },
            )

            AnimatedContent(
                targetState = currentSubScreen,
                label = "Contact subscreen"
            ) {
                if (it == ContactSubScreenState.MESSAGES) {
                    ContactSubScreen(
                        currentSubScreen = it,
                        listState = listStateMessageScreen,
                        previousIndex = previousIndexMessageScreen,
                        onScroll = { visible, previousIndex ->
                            topNavigationVisible = visible
                            previousIndexMessageScreen.intValue = previousIndex
                        },
                    )
                } else {
                    ContactSubScreen(
                        currentSubScreen = ContactSubScreenState.FRIENDS,
                        listState = listStateFriendScreen,
                        previousIndex = previousIndexFriendScreen,
                        onScroll = { visible, previousIndex ->
                            topNavigationVisible = visible
                            previousIndexFriendScreen.intValue = previousIndex
                        },
                    )
                }
            }
        }
    }
}

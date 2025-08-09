package com.apcs.worknestapp.ui.screens.contact

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

@OptIn(ExperimentalMaterial3Api::class)
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
                    IconButton(onClick = { navController.navigate(Screen.AddContact.route) }) {
                        Icon(
                            painter = painterResource(R.drawable.outline_add_user),
                            contentDescription = "Add more contact",
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .animateContentSize()
                .padding(innerPadding),
        ) {
            ContactTopNavigation(
                currentSubScreen = currentSubScreen,
                visible = true,
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
                ContactSubScreen(currentSubScreen = it)
            }
        }
    }
}

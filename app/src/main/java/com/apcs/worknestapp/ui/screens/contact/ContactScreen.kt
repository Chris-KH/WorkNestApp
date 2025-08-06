package com.apcs.worknestapp.ui.screens.contact

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
import androidx.compose.runtime.remember
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
        val listState = rememberLazyListState()
        var topNavigationVisible by rememberSaveable { mutableStateOf(true) }
        var previousIndex by remember { mutableIntStateOf(0) }

        LaunchedEffect(listState) {
            snapshotFlow { listState.firstVisibleItemIndex }
                .collect { currentIndex ->
                    topNavigationVisible = currentIndex < previousIndex || currentIndex == 0
                    previousIndex = currentIndex
                }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .animateContentSize()
                .padding(innerPadding),
        ) {
            TopNavigation(
                visible = topNavigationVisible,
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
            ) {
                items(50) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(8.dp)
                            .background(Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Item $it", color = Color.White)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}

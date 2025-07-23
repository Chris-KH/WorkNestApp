package com.apcs.worknestapp.ui.screens.notification

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.components.bottombar.MainBottomBar
import com.apcs.worknestapp.ui.components.topbar.TopBarNotificationScreen
import com.apcs.worknestapp.ui.screens.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    var messages by remember { mutableStateOf<List<String>>(List(size = 0) { "Hehe" }) }
    var isRefreshing by remember { mutableStateOf(false) }

    fun refreshNotifications() {
        isRefreshing = true
        coroutineScope.launch {
            delay(5000)
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            TopBarNotificationScreen(
                actions = {
                    IconButton(
                        onClick = {

                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.symbol_three_dot),
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(-90f)
                        )
                    }
                }
            )
        },
        bottomBar = {
            MainBottomBar(
                currentScreen = Screen.Notification,
                navController = navController,
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        if (messages.isEmpty()) {
            EmptyNotification(
                isRefreshing = isRefreshing,
                onRefresh = { refreshNotifications() },
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    refreshNotifications()
                },
                modifier = modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                LazyColumn {
                    itemsIndexed(items = messages) { idx, item ->
                        Text(text = item)
                    }
                }
            }
        }
    }
}

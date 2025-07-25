package com.apcs.worknestapp.ui.screens.notification

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.notification.NotificationViewModel
import com.apcs.worknestapp.ui.components.bottombar.MainBottomBar
import com.apcs.worknestapp.ui.components.topbar.TopBarNotificationScreen
import com.apcs.worknestapp.ui.screens.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
    notificationViewModel: NotificationViewModel = hiltViewModel(),
) {
    val notifications = notificationViewModel.notifications.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    var showModalBottom by remember { mutableStateOf(false) }

    fun refreshNotifications() {
        isRefreshing = true
        coroutineScope.launch {
            notificationViewModel.refreshNotifications()
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            TopBarNotificationScreen(
                actions = {
                    IconButton(onClick = {
                        //TODO Mark all read
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.outline_checkmark),
                            contentDescription = null,
                            tint = if (showModalBottom) MaterialTheme.colorScheme.outline
                            else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    IconButton(onClick = { showModalBottom = true }) {
                        Icon(
                            painter = painterResource(R.drawable.symbol_three_dot),
                            contentDescription = null,
                            tint = if (showModalBottom) MaterialTheme.colorScheme.outline
                            else MaterialTheme.colorScheme.primary,
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
        if (showModalBottom) {
            NotificationModalBottom(
                onDismissRequest = { showModalBottom = false }
            )
        }

        if (notifications.value.isEmpty()) {
            EmptyNotification(
                isRefreshing = isRefreshing,
                onRefresh = { refreshNotifications() },
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { refreshNotifications() },
                modifier = modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    itemsIndexed(
                        items = notifications.value,
                        key = { _, item -> item.docId.hashCode() }
                    ) { idx, item ->
                        NotificationItem(notification = item)
                        if (idx + 1 < notifications.value.size) Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

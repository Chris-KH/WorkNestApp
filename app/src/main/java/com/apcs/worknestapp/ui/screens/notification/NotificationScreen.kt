package com.apcs.worknestapp.ui.screens.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.notification.NotificationViewModel
import com.apcs.worknestapp.ui.components.LoadingScreen
import com.apcs.worknestapp.ui.components.bottombar.MainBottomBar
import com.apcs.worknestapp.ui.components.topbar.MainTopBar
import com.apcs.worknestapp.ui.screens.Screen
import kotlinx.coroutines.launch
import java.util.UUID

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
    var isFirstLoad by rememberSaveable { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var showModalBottom by remember { mutableStateOf(false) }

    fun refreshNotifications() {
        isRefreshing = true
        coroutineScope.launch {
            notificationViewModel.refreshNotifications()
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        if (isFirstLoad) {
            notificationViewModel.refreshNotificationsIfEmpty()
            isFirstLoad = false
        }
    }

    Scaffold(
        topBar = {
            MainTopBar(
                title = "Notifications",
                actions = {
                    IconButton(
                        enabled = notifications.value.isNotEmpty() && !showModalBottom,
                        onClick = {
                            coroutineScope.launch {
                                val isSuccess = notificationViewModel.markAllRead()
                                if (isSuccess) {
                                    snackbarHost.showSnackbar(
                                        message = "Mark all read successful",
                                        withDismissAction = true,
                                    )
                                } else {
                                    snackbarHost.showSnackbar(
                                        message = "Something went wrong. Mark all read failed",
                                        withDismissAction = true,
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_checkmark),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    //TODO
//                    IconButton(
//                        enabled = !showModalBottom,
//                        onClick = { showModalBottom = true },
//                    ) {
//                        Icon(
//                            painter = painterResource(R.drawable.symbol_three_dot),
//                            contentDescription = null,
//                            modifier = Modifier
//                                .size(24.dp)
//                                .rotate(-90f)
//                        )
//                    }
                }
            )
        },
        bottomBar = {
            MainBottomBar(
                currentScreen = Screen.Notification,
                navController = navController,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) { innerPadding ->
        if (showModalBottom) {
            NotificationModalBottom(
                onDismissRequest = { showModalBottom = false }
            )
        }

        if (isFirstLoad) {
            LoadingScreen(modifier = Modifier.padding(innerPadding))
        } else if (notifications.value.isEmpty()) {
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
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(
                        items = notifications.value,
                        key = { it.docId ?: UUID.randomUUID() }
                    ) { notification ->
                        NotificationItem(
                            notification = notification,
                            onClick = { notificationId ->
                                coroutineScope.launch {
                                    notificationViewModel.markRead(notificationId, true)
                                }
                            },
                            onMarkRead = { notificationId, read ->
                                coroutineScope.launch {
                                    val isSuccess =
                                        notificationViewModel.markRead(notificationId, read)
                                    if (!isSuccess) {
                                        snackbarHost.showSnackbar(
                                            message = "Delete notification failed",
                                            withDismissAction = true,
                                        )
                                    }
                                }
                            },
                            onDelete = { notificationId ->
                                coroutineScope.launch {
                                    if (notificationId == null) {
                                        snackbarHost.showSnackbar(
                                            message = "Delete notification failed",
                                            withDismissAction = true,
                                        )
                                    } else {
                                        val isSuccess =
                                            notificationViewModel.deleteNotification(notificationId)
                                        if (isSuccess) {
                                            snackbarHost.showSnackbar(
                                                message = "Notification successfully deleted",
                                                withDismissAction = true,
                                            )
                                        } else {
                                            snackbarHost.showSnackbar(
                                                message = "Delete notification failed",
                                                withDismissAction = true,
                                            )
                                        }
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

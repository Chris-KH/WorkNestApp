package com.apcs.worknestapp.ui.screens.contact

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavHostController
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.message.Conservation
import com.apcs.worknestapp.data.remote.message.MessageViewModel
import com.apcs.worknestapp.data.remote.user.UserViewModel
import com.apcs.worknestapp.ui.components.RotatingIcon
import com.apcs.worknestapp.ui.screens.Screen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactSubScreen(
    currentSubScreen: ContactSubScreenState,
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    listState: LazyListState,
    isFirstLoad: Boolean,
    onFirstLoadDone: () -> Unit,
    userViewModel: UserViewModel = hiltViewModel(),
    messageViewModel: MessageViewModel = hiltViewModel(),
) {
    val authId = FirebaseAuth.getInstance().currentUser?.uid
    var isRefreshing by remember(currentSubScreen) { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val friends = userViewModel.friends.collectAsState()
    val conservations = messageViewModel.conservations.collectAsState()

    LaunchedEffect(Unit) {
        if (isFirstLoad) {
            val isSuccess = when(currentSubScreen) {
                ContactSubScreenState.FRIENDS -> userViewModel.loadFriendsIfEmpty()
                ContactSubScreenState.MESSAGES -> messageViewModel.loadConservationsIfEmpty()
            }
            onFirstLoadDone()
            if (!isSuccess) {
                snackbarHost.showSnackbar(
                    message = "Load data failed. Something went wrong",
                    withDismissAction = true,
                )
            }
        }
    }

    LifecycleResumeEffect(Unit) {
        when(currentSubScreen) {
            ContactSubScreenState.FRIENDS -> {}
            ContactSubScreenState.MESSAGES -> messageViewModel.registerConservationListener()
        }



        onPauseOrDispose {
            when(currentSubScreen) {
                ContactSubScreenState.FRIENDS -> {}
                ContactSubScreenState.MESSAGES -> messageViewModel.removeConservationListener()
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                isRefreshing = true
                val isSuccess = when(currentSubScreen) {
                    ContactSubScreenState.FRIENDS -> userViewModel.loadFriends()
                    ContactSubScreenState.MESSAGES -> messageViewModel.loadConservations()
                }
                isRefreshing = false
                if (!isSuccess) {
                    snackbarHost.showSnackbar(
                        message = "Refresh failed",
                        withDismissAction = true,
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
    ) {
        if (isFirstLoad) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                RotatingIcon(
                    painter = painterResource(R.drawable.loading_icon_6),
                    contentDescription = "Loading",
                    tint = MaterialTheme.colorScheme.onBackground,
                    duration = 3000,
                    modifier = Modifier.size(48.dp)
                )
            }
        } else {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(vertical = 4.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                when(currentSubScreen) {
                    ContactSubScreenState.MESSAGES -> {
                        items(
                            items = conservations.value.filter { !it.isTemporary },
                            key = { it.docId!! }
                        ) {
                            ConservationItem(
                                conservation = it,
                                modifier = Modifier,
                                onMarkSeenState = { state ->
                                    if (it.docId == null) return@ConservationItem
                                    coroutineScope.launch {
                                        messageViewModel.updateConservationSeen(it.docId, state)
                                    }
                                },
                                onDelete = {},
                                onClick = {
                                    val conservationId = it.docId
                                    messageViewModel.getConservation(docId = conservationId)
                                    navController.navigate(
                                        Screen.Chat.route.replace(
                                            "{conservationId}",
                                            conservationId ?: ""
                                        )
                                    )
                                },
                            )
                        }
                    }

                    ContactSubScreenState.FRIENDS -> {
                        items(
                            items = friends.value.sortedBy { it.name },
                            key = { it.docId!! }
                        ) {
                            FriendItem(
                                friend = it,
                                modifier = Modifier,
                                onClick = {
                                    if (it.docId != null) {
                                        navController.navigate(
                                            Screen.UserProfile.route.replace(
                                                "{userId}", it.docId
                                            )
                                        ) {
                                            restoreState = true
                                            launchSingleTop = true
                                        }
                                    }

                                },
                                onMessage = {
                                    val userId = it.docId
                                    if (authId == null || userId == null) return@FriendItem
                                    val conservation =
                                        messageViewModel.getConservationWith(userId)
                                    if (conservation?.docId != null) {
                                        navController.navigate(
                                            Screen.Chat.route.replace(
                                                "{conservationId}", conservation.docId
                                            )
                                        ) {
                                            restoreState = true
                                            launchSingleTop = true
                                        }
                                    } else {
                                        val userIds = listOf(authId, userId).sorted()
                                        val docId = userIds.joinToString("_")
                                        val newConservation = Conservation(
                                            docId = docId,
                                            userIds = userIds,
                                            senderSeen = null, receiverSeen = null,
                                            lastContent = null, lastTime = null,
                                            isTemporary = true,
                                        )
                                        val isSuccess = messageViewModel.createConservation(
                                            newConservation, it
                                        )
                                        if (isSuccess) {
                                            messageViewModel.getConservationWith(userId)
                                            navController.navigate(
                                                Screen.Chat.route.replace(
                                                    "{conservationId}", docId
                                                )
                                            ) {
                                                restoreState = true
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                },
                                onDelete = {
                                    coroutineScope.launch {
                                        if (it.docId == null || authId == null) return@launch
                                        val docId = listOf(it.docId, authId)
                                            .sorted().joinToString("_")
                                        val isSuccess = userViewModel.deleteFriendship(docId)
                                        if (!isSuccess) {
                                            snackbarHost.showSnackbar(
                                                message = "Unfriend with ${it.name} failed",
                                                withDismissAction = true,
                                            )
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
}

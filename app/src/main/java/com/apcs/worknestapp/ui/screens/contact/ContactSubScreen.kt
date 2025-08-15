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
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.message.MessageViewModel
import com.apcs.worknestapp.data.remote.user.UserViewModel
import com.apcs.worknestapp.ui.components.RotatingIcon
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactSubScreen(
    currentSubScreen: ContactSubScreenState,
    snackbarHost: SnackbarHostState,
    listState: LazyListState,
    isFirstLoad: Boolean,
    onFirstLoadDone: () -> Unit,
    userViewModel: UserViewModel = hiltViewModel(),
    messageViewModel: MessageViewModel = hiltViewModel(),
) {
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                when(currentSubScreen) {
                    ContactSubScreenState.MESSAGES -> {
                        items(
                            items = conservations.value,
                            key = { it.docId!! }
                        ) {
                            ConservationItem(
                                conservation = it,
                                modifier = Modifier,
                                onClick = {},
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
                                onClick = {},
                            )
                        }
                    }
                }
            }
        }
    }
}

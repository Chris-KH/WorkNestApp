package com.apcs.worknestapp.ui.screens.contact

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ripple
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.message.MessageViewModel
import com.apcs.worknestapp.data.remote.user.User
import com.apcs.worknestapp.data.remote.user.UserViewModel
import com.apcs.worknestapp.ui.components.RotatingIcon
import com.apcs.worknestapp.ui.theme.Roboto
import kotlinx.coroutines.delay
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

    LaunchedEffect(Unit) {
        if (isFirstLoad) {
            when(currentSubScreen) {
                ContactSubScreenState.MESSAGES -> delay(1000)
                ContactSubScreenState.FRIENDS -> userViewModel.loadFriendsIfEmpty()
            }
            onFirstLoadDone()
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                isRefreshing = true
                val isSuccess = when(currentSubScreen) {
                    ContactSubScreenState.MESSAGES -> delay(10000)
                    ContactSubScreenState.FRIENDS -> userViewModel.loadFriends()
                }
                isRefreshing = false
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
                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 0.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                when(currentSubScreen) {
                    ContactSubScreenState.MESSAGES -> {}
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

@Composable
fun FriendItem(
    friend: User,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = {},
                indication = ripple(color = MaterialTheme.colorScheme.primary),
                interactionSource = interactionSource,
            )
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 12.dp),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(friend.avatar)
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.fade_avatar_fallback),
            error = painterResource(R.drawable.fade_avatar_fallback),
            contentDescription = "Preview avatar",
            contentScale = ContentScale.Crop,
            filterQuality = FilterQuality.Low,
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = friend.name ?: "",
            fontSize = 16.sp,
            lineHeight = 16.sp,
            fontFamily = Roboto,
            fontWeight = FontWeight.Normal,
        )
    }
}

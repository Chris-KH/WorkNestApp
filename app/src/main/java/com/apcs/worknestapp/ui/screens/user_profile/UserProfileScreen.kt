package com.apcs.worknestapp.ui.screens.user_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.apcs.worknestapp.LocalAuthViewModel
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.user.User
import com.apcs.worknestapp.data.remote.user.UserViewModel
import com.apcs.worknestapp.ui.components.ProfileInfoCard
import com.apcs.worknestapp.ui.theme.Roboto
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: String,
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
    userViewModel: UserViewModel = hiltViewModel(),
) {
    val authViewModel = LocalAuthViewModel.current
    var user by remember { mutableStateOf<User?>(null) }

    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val pullRefreshState = rememberPullToRefreshState()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        user = userViewModel.getUser(userId)
    }

    Scaffold(
        topBar = {
            val currentVisibleIndex =
                remember { derivedStateOf { listState.firstVisibleItemIndex } }

            val topBarColor = if (currentVisibleIndex.value == 0) Color.Transparent
            else MaterialTheme.colorScheme.surface

            TopAppBar(
                title = {
                    Text(
                        text =
                            if (currentVisibleIndex.value == 0) ""
                            else user?.name ?: "",
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Roboto,
                        fontSize = 16.sp,
                        lineHeight = 16.sp,
                        letterSpacing = (0).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(R.drawable.symbol_angle_arrow),
                            contentDescription = "back",
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(90f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarColor,
                    scrolledContainerColor = topBarColor,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                expandedHeight = 52.dp,
                modifier = Modifier
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        PullToRefreshBox(
            state = pullRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = {
                coroutineScope.launch {
                    isRefreshing = true
                    val refreshUser = userViewModel.refreshUser(userId)
                    isRefreshing = false
                    if (refreshUser == null) {
                        snackbarHost.showSnackbar(
                            message = "Refresh profile failed",
                            withDismissAction = true,
                            duration = SnackbarDuration.Short,
                        )
                    } else user = refreshUser
                }
            },
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .padding(
                    start = innerPadding.calculateLeftPadding(LayoutDirection.Ltr),
                    end = innerPadding.calculateRightPadding(LayoutDirection.Ltr),
                    bottom = innerPadding.calculateBottomPadding(),
                )
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item {
                    val topPadding = innerPadding.calculateTopPadding()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(topPadding + 60.dp)
                            .background(Color.DarkGray)
                    )
                }

                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .background(Color.DarkGray)
                        )
                        UserProfileHeader(
                            userName = user?.name,
                            userEmail = user?.email,
                            imageUrl = user?.avatar,
                            avatarSize = 120.dp,
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(
                            onClick = {},
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_chat),
                                contentDescription = "Chat with user",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Message",
                                fontSize = 16.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = Roboto,
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.inverseSurface,
                                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                            )
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_remove_user),
                                contentDescription = "Add friend",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                item {
                    ProfileInfoCard(
                        bio = user?.bio,
                        createdAt = user?.createdAt,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                item { Spacer(modifier = Modifier.height(400.dp)) }
            }
        }
    }
}

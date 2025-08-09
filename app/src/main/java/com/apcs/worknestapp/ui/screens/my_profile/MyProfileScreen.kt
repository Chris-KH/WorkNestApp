package com.apcs.worknestapp.ui.screens.my_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.apcs.worknestapp.LocalAuthViewModel
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.components.ProfileInfoCard
import com.apcs.worknestapp.ui.components.bottombar.MainBottomBar
import com.apcs.worknestapp.ui.components.topbar.MainTopBar
import com.apcs.worknestapp.ui.screens.Screen
import com.apcs.worknestapp.ui.theme.Inter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val authViewModel = LocalAuthViewModel.current
    val profile = authViewModel.profile.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val pullRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            MainTopBar(
                title = profile.value?.name ?: "My Profile",
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.Setting.route)
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.outline_gear),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
            )
        },
        bottomBar = {
            MainBottomBar(
                currentScreen = Screen.MyProfile,
                navController = navController,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) { innerPadding ->
        PullToRefreshBox(
            state = pullRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = {
                coroutineScope.launch {
                    isRefreshing = true
                    val isSuccess = authViewModel.loadUserProfile()
                    isRefreshing = false
                    if (!isSuccess) {
                        snackbarHost.showSnackbar(
                            message = "Refresh your profile failed",
                            withDismissAction = true,
                            duration = SnackbarDuration.Short,
                        )
                    }
                }
            },
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 24.dp, horizontal = 16.dp),
            ) {
                item {
                    MyProfileHeader(
                        userId = profile.value?.docId,
                        userName = profile.value?.name,
                        userEmail = profile.value?.email,
                        imageUrl = profile.value?.avatar,
                        avatarSize = 120.dp,
                        snackbarHost = snackbarHost,
                    )
                }

                item { Spacer(modifier = Modifier.height(30.dp)) }

                item {
                    Button(
                        onClick = { navController.navigate(Screen.EditProfile.route) },
                        contentPadding = PaddingValues(vertical = 0.dp),
                        shape = RoundedCornerShape(50f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painterResource(R.drawable.fill_edit_pen),
                            contentDescription = "Edit profile",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Edit profile",
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = Inter,
                            fontSize = 14.sp,
                            lineHeight = 14.sp,
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                item {
                    ProfileInfoCard(
                        bio = profile.value?.bio,
                        createdAt = profile.value?.createdAt,
                    )
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

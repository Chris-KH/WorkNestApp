package com.apcs.worknestapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.pullToRefreshIndicator
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.apcs.worknestapp.LocalAuthViewModel
import com.apcs.worknestapp.ui.screens.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val authViewModel = LocalAuthViewModel.current
    val profile = authViewModel.profile.collectAsState()
    val scrollState = rememberScrollState()
    val overscrollEffect = rememberOverscrollEffect()

    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val pullRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                val isSuccess = authViewModel.loadUserProfile()
                isRefreshing = false
                if (!isSuccess) {
                    snackbarHost.showSnackbar(
                        message = "Refresh user profile fail",
                        withDismissAction = true,
                        duration = SnackbarDuration.Short,
                    )
                }
            }
        },
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullRefreshState,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = MaterialTheme.colorScheme.surface,
                color = MaterialTheme.colorScheme.primary
            )
        },
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(
                    state = scrollState,
                    overscrollEffect = overscrollEffect,
                )
                .padding(top = 12.dp)
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProfileHeader(
                imageUrl = profile.value?.avatar,
                name = profile.value?.name,
                email = profile.value?.email,
            )

            Spacer(modifier = Modifier.height(16.dp))
            EditProfileButton(
                onClick = {
                    navController.navigate(Screen.EditProfile.route)
                }
            )
            Spacer(modifier = Modifier.height(24.dp))

            ProfileInfoCard(
                bio = profile.value?.bio,
                createdAt = profile.value?.createdAt,
            )
        }
    }

    /* Box(
         modifier = modifier
             .fillMaxSize()
             .background(MaterialTheme.colorScheme.background)
             .verticalScroll(
                 state = scrollState,
                 overscrollEffect = overscrollEffect,
             )
     ) {
         Column(
             modifier = Modifier
                 .fillMaxSize()
                 .padding(top = 12.dp)
                 .padding(horizontal = 12.dp),
             horizontalAlignment = Alignment.CenterHorizontally,
         ) {
             ProfileHeader(
                 imageUrl = profile.value?.avatar,
                 name = profile.value?.name,
                 email = profile.value?.email,
             )

             Spacer(modifier = Modifier.height(16.dp))
             EditProfileButton(onClick = { navController.navigate(Screen.EditProfile.route) })
             Spacer(modifier = Modifier.height(24.dp))

             ProfileInfoCard(
                 bio = profile.value?.bio,
                 createdAt = profile.value?.createdAt,
             )
         }
     }*/
}

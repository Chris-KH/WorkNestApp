package com.apcs.worknestapp.ui.screens.edit_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.apcs.worknestapp.LocalAuthViewModel
import com.apcs.worknestapp.ui.components.AvatarPicker
import com.apcs.worknestapp.ui.components.topbar.ExitOnlyTopBar
import com.apcs.worknestapp.ui.screens.Screen
import com.apcs.worknestapp.ui.screens.edit_profile.EditProfileField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val authViewModel = LocalAuthViewModel.current
    val profile = authViewModel.profile.collectAsState()

    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        authViewModel.loadUserProfile()
        isLoading = false
    }

    Scaffold(
        topBar = {
            ExitOnlyTopBar(
                navController = navController,
                screen = Screen.EditProfile,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) { innerPadding ->
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AvatarPicker(
                    userId = profile.value?.docId,
                    imageUrl = profile.value?.avatar,
                    isLoading = isLoading,
                    snackbarHost = snackbarHost,
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = (0.5).dp,
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    EditItemColumn(
                        items = listOf(
                            EditProfileField.NAME.fieldName to profile.value?.name,
                            EditProfileField.PRONOUNS.fieldName to profile.value?.pronouns,
                            EditProfileField.BIO.fieldName to profile.value?.bio,
                        ),
                        onItemClick = { field ->
                            navController.navigate(
                                Screen.EditProfileDetail.route.replace(
                                    "{field}",
                                    field
                                )
                            )
                        }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = (0.5).dp,
                    )
                }
            }
        }
    }
}

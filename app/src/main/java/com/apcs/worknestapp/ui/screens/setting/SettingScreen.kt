package com.apcs.worknestapp.ui.screens.setting

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.apcs.worknestapp.LocalAuthViewModel
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.components.ConfirmDialog
import com.apcs.worknestapp.ui.components.inputfield.SearchInput
import com.apcs.worknestapp.ui.components.topbar.ExitOnlyTopBar
import com.apcs.worknestapp.ui.screens.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val authViewModel = LocalAuthViewModel.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var showConfirmDialog by remember { mutableStateOf(false) }

    var searchValue by remember { mutableStateOf("") }
    val searchInteractionSource = remember { MutableInteractionSource() }

    val horizontalPadding = 12.dp

    Scaffold(
        topBar = {
            ExitOnlyTopBar(
                navController = navController,
                screen = Screen.Setting,
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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (showConfirmDialog) {
                ConfirmDialog(
                    title = "Sign Out",
                    message = "Are you sure you want to log out?",
                    confirmText = "Sign Out",
                    cancelText = "Cancel",
                    onDismissRequest = { showConfirmDialog = false },
                    onConfirm = {
                        coroutineScope.launch {
                            showConfirmDialog = false
                            authViewModel.signOut()
                        }
                    },
                    onCancel = { showConfirmDialog = false },
                )
            }

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                SearchInput(
                    value = searchValue,
                    onValueChange = { searchValue = it },
                    onCancel = {
                        focusManager.clearFocus()
                        searchValue = ""
                    },
                    interactionSource = searchInteractionSource,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding)
                        .padding(top = 8.dp),
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding, vertical = 16.dp),
                ) {
                    Text(
                        text = "Account setting",
                        fontSize = 14.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingRow(
                        text = "Account",
                        icon = R.drawable.outline_account,
                        shape = RoundedCornerShape(30f),
                        onClick = {
                            navController.navigate(
                                Screen.SettingDetail.route.replace(
                                    "{field}",
                                    "Account"
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider(
                    thickness = 6.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding, vertical = 16.dp),
                ) {
                    Text(
                        text = "App setting",
                        fontSize = 14.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingRow(
                        text = "Theme",
                        icon = R.drawable.outline_palette,
                        shape = RoundedCornerShape(30f),
                        onClick = {
                            navController.navigate(
                                Screen.SettingDetail.route.replace(
                                    "{field}",
                                    "Theme"
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingRow(
                        text = "Language",
                        icon = R.drawable.outline_language,
                        shape = RoundedCornerShape(30f),
                        onClick = {
                            navController.navigate(
                                Screen.SettingDetail.route.replace(
                                    "{field}",
                                    "Language"
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingRow(
                        text = "Notification",
                        icon = R.drawable.outline_notification,
                        shape = RoundedCornerShape(30f),
                        onClick = {
                            navController.navigate(
                                Screen.SettingDetail.route.replace(
                                    "{field}",
                                    "Notification"
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider(
                    thickness = 6.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding, vertical = 16.dp),
                ) {
                    Text(
                        text = "Information",
                        fontSize = 14.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingRow(
                        text = "About",
                        icon = R.drawable.outline_info,
                        shape = RoundedCornerShape(30f),
                        onClick = {
                            navController.navigate(
                                Screen.SettingDetail.route.replace(
                                    "{field}",
                                    "About"
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    SettingRow(
                        text = "Sign Out",
                        icon = R.drawable.outline_logout,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.error,
                        showArrow = false,
                        shape = RoundedCornerShape(30f),
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

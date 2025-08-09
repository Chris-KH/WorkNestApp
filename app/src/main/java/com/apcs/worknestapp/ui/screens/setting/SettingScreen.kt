package com.apcs.worknestapp.ui.screens.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.apcs.worknestapp.LocalAuthViewModel
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.components.ConfirmDialog
import com.apcs.worknestapp.ui.components.topbar.CenterExitOnlyTopBar
import com.apcs.worknestapp.ui.screens.Screen
import com.apcs.worknestapp.ui.theme.Roboto
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val authViewModel = LocalAuthViewModel.current
    val coroutineScope = rememberCoroutineScope()

    var showConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterExitOnlyTopBar(
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
            val horizontalPadding = 12.dp
            val labelStyle = TextStyle(
                fontSize = 14.sp, lineHeight = 14.sp, letterSpacing = 0.sp,
                fontWeight = FontWeight.Medium, fontFamily = Roboto,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val rowShape = RoundedCornerShape(30f)
            val columnModifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = 16.dp)

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

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Column(modifier = columnModifier) {
                        Text(text = "Account", style = labelStyle)
                        Spacer(modifier = Modifier.height(8.dp))
                        SettingRow(
                            text = "Account",
                            icon = R.drawable.outline_account,
                            shape = rowShape,
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
                }

                item {
                    HorizontalDivider(
                        thickness = 6.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }

                item {
                    Column(modifier = columnModifier) {
                        Text(text = "App", style = labelStyle)
                        Spacer(modifier = Modifier.height(8.dp))
                        SettingRow(
                            text = "Theme",
                            icon = R.drawable.outline_palette,
                            shape = rowShape,
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
                            shape = rowShape,
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
                            shape = rowShape,
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
                }

                item {
                    HorizontalDivider(
                        thickness = 6.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }

                item {
                    Column(modifier = columnModifier) {
                        Text(text = "Information", style = labelStyle)
                        Spacer(modifier = Modifier.height(8.dp))
                        SettingRow(
                            text = "About",
                            icon = R.drawable.outline_info,
                            shape = rowShape,
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
                            shape = rowShape,
                            onClick = { showConfirmDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

package com.apcs.worknestapp.ui.screens.setting_detail.setting_account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.apcs.worknestapp.LocalAuthViewModel
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.components.DiscardChangeDialog
import com.apcs.worknestapp.ui.components.topbar.CustomTopBar
import com.apcs.worknestapp.ui.theme.Roboto
import kotlinx.coroutines.launch
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingAccountDetailScreen(
    field: SettingAccountField,
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val authViewModel = LocalAuthViewModel.current
    val profile = authViewModel.profile.collectAsState()

    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    var showAlert by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    var initialValue by remember {
        when(field) {
            SettingAccountField.NAME -> mutableStateOf(profile.value?.name ?: "")
            SettingAccountField.EMAIL -> mutableStateOf(profile.value?.email ?: "")
            SettingAccountField.PHONE -> mutableStateOf(profile.value?.phone ?: "")
            SettingAccountField.ADDRESS -> mutableStateOf(profile.value?.address ?: "")
        }
    }
    var value by remember { mutableStateOf(initialValue) }

    val description = when(field) {
        SettingAccountField.NAME,
            -> """
                Help people discover your account by using the name you're known by:
                either your full name, nickname or business name
                """.trimIndent().replace("\n", " ")

        SettingAccountField.EMAIL,
            -> "Your email is linked to your account and can’t be changed for security reasons"

        SettingAccountField.PHONE,
            -> "Add a phone number to help secure your account and make it easier to recover"

        SettingAccountField.ADDRESS,
            -> "Add your address to receive location-based suggestions and services"
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                field = field.fieldName,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (value == initialValue) {
                                focusRequester.freeFocus()
                                navController.popBackStack()
                            } else showAlert = true
                        },
                        enabled = !isSubmitting,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.symbol_angle_arrow),
                            contentDescription = "back",
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(90f)
                        )
                    }
                },
                actions = {
                    if (!isSubmitting) {
                        TextButton(
                            onClick = {
                                isSubmitting = true
                                coroutineScope.launch {
                                    val isSuccess = when(field) {
                                        SettingAccountField.NAME,
                                            -> authViewModel.updateUserName(value)

                                        SettingAccountField.EMAIL -> false

                                        SettingAccountField.PHONE,
                                            -> authViewModel.updateUserPhone(value)

                                        SettingAccountField.ADDRESS,
                                            -> authViewModel.updateUserAddress(value)
                                    }
                                    isSubmitting = false
                                    if (isSuccess) {
                                        focusRequester.freeFocus()
                                        navController.popBackStack()
                                    } else {
                                        snackbarHost.showSnackbar(
                                            message = "Update ${field.fieldName.lowercase()} failed",
                                            withDismissAction = true,
                                        )
                                    }
                                }
                            },
                            enabled = (value != initialValue),
                        ) {
                            Text(
                                text = "Done",
                                fontSize = 14.sp,
                                lineHeight = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .imePadding(),
        ) {
            if (showAlert) {
                DiscardChangeDialog(
                    onDismissRequest = { showAlert = false },
                    onDiscard = {
                        showAlert = false
                        focusRequester.freeFocus()
                        navController.popBackStack()
                    },
                    onCancel = { showAlert = false }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = {
                            if (field == SettingAccountField.EMAIL) return@OutlinedTextField
                            value = if (field == SettingAccountField.ADDRESS) {
                                it.substring(0, min(150, it.length))
                            } else it
                        },
                        enabled = !isSubmitting,
                        label = {
                            Text(
                                text = field.fieldName,
                                fontSize = 14.sp,
                                lineHeight = 14.sp,
                            )
                        },
                        placeholder = {
                            Text(
                                text = "Type something...",
                                fontSize = 14.sp,
                                lineHeight = 14.sp,
                            )
                        },
                        textStyle = TextStyle(
                            fontFamily = Roboto,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            lineHeight = 14.sp,
                            letterSpacing = 0.sp,
                        ),
                        shape = RoundedCornerShape(40f),
                        singleLine = (field != SettingAccountField.ADDRESS),
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = description,
                        fontSize = 13.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }

                if (field == SettingAccountField.ADDRESS) {
                    Text(
                        text = "${value.length} / 150",
                        fontSize = 12.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

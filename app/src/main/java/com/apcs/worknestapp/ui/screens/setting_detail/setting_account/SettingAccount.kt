package com.apcs.worknestapp.ui.screens.setting_detail.setting_account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.apcs.worknestapp.LocalAuthViewModel
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.screens.Screen

@Composable
fun SettingAccount(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val horizontalPadding = 12.dp

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
                .padding(top = 16.dp)
        ) {
            AccountInformation(
                navController = navController,
            )
        }
    }
}

@Composable
fun AccountInformation(
    navController: NavHostController,
) {
    val authViewModel = LocalAuthViewModel.current
    val profile = authViewModel.profile.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "Account Information",
            fontSize = 14.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.outline,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(15f),
                ),
        ) {
            SettingAccountItem(
                label = "Name",
                value = profile.value?.name ?: "",
                onClick = {
                    navController.navigate(
                        Screen.SettingAccount.route.replace(
                            "{field}",
                            SettingAccountField.NAME.fieldName
                        )
                    )
                },
            )
            SettingAccountItem(
                label = "Email",
                value = profile.value?.email ?: "",
                onClick = {
                    navController.navigate(
                        Screen.SettingAccount.route.replace(
                            "{field}",
                            SettingAccountField.EMAIL.fieldName
                        )
                    )
                },
            )
            SettingAccountItem(
                label = "Phone",
                value = profile.value?.phone ?: "",
                onClick = {
                    navController.navigate(
                        Screen.SettingAccount.route.replace(
                            "{field}",
                            SettingAccountField.PHONE.fieldName
                        )
                    )
                },
            )
            SettingAccountItem(
                label = "Address",
                value = profile.value?.address ?: "",
                onClick = {
                    navController.navigate(
                        Screen.SettingAccount.route.replace(
                            "{field}",
                            SettingAccountField.ADDRESS.fieldName
                        )
                    )
                },
            )
        }
    }
}

@Composable
fun SettingAccountItem(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            lineHeight = 14.sp,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            lineHeight = 12.sp,
            color = MaterialTheme.colorScheme.outline,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            painter = painterResource(R.drawable.symbol_angle_arrow),
            contentDescription = null,
            modifier = Modifier
                .size(20.dp)
                .rotate(-90f)
        )
    }
}

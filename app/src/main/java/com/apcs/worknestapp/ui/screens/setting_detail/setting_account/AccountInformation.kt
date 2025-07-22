package com.apcs.worknestapp.ui.screens.setting_detail.setting_account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.apcs.worknestapp.LocalAuthViewModel
import com.apcs.worknestapp.ui.screens.Screen

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
                    shape = RoundedCornerShape(30f),
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

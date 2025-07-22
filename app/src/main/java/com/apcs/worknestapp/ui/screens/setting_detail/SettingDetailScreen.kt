package com.apcs.worknestapp.ui.screens.setting_detail

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.components.topbar.CustomTopBar
import com.apcs.worknestapp.ui.screens.setting.SettingField
import com.apcs.worknestapp.ui.screens.setting_detail.setting_account.SettingAccount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingDetailScreen(
    field: SettingField,
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
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
                            navController.popBackStack()
                        },
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
                actions = {}
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) { innerPadding ->
        when(field) {
            SettingField.ACCOUNT -> SettingAccount(
                navController = navController,
                snackbarHost = snackbarHost,
                modifier = Modifier.padding(innerPadding),
            )

            SettingField.THEME -> SettingTheme(
                modifier = Modifier.padding(innerPadding),
            )

            SettingField.LANGUAGE -> SettingLanguage(
                modifier = Modifier.padding(innerPadding),
            )

            SettingField.NOTIFICATION -> SettingNotification(
                navController = navController,
                snackbarHost = snackbarHost,
                modifier = Modifier.padding(innerPadding),
            )

            SettingField.ABOUT -> SettingAbout(
                navController = navController,
                snackbarHost = snackbarHost,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

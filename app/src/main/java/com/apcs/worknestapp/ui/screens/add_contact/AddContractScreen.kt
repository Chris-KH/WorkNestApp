package com.apcs.worknestapp.ui.screens.add_contact

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavHostController
import com.apcs.worknestapp.ui.components.topbar.SearchTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContractScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    var searchValue by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            SearchTopBar(
                value = searchValue,
                onValueChange = { searchValue = it },
                onCancel = { focusManager.clearFocus() },
                navController = navController,
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            items(100) {
                Text("hello")
            }
        }
    }
}

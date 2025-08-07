package com.apcs.worknestapp.ui.screens.add_contact

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.user.User
import com.apcs.worknestapp.data.remote.user.UserViewModel
import com.apcs.worknestapp.ui.components.RotatingIcon
import com.apcs.worknestapp.ui.components.topbar.SearchTopBar
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun AddContractScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
    userViewModel: UserViewModel = hiltViewModel(),
) {
    val focusManager = LocalFocusManager.current
    var searchValue by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    val userList = remember { mutableStateListOf<User>() }
    val listCache = remember { mutableStateMapOf<String, List<User>>() }

    LaunchedEffect(Unit) {
        launch {
            snapshotFlow { searchValue }
                .debounce(250)
                .distinctUntilChanged()
                .collectLatest { query ->
                    isSearching = true
                    if (query.isBlank()) {
                        userList.clear()
                    } else {
                        val result = listCache[searchValue] ?: userViewModel.findUsers(query)
                        userList.clear()
                        userList.addAll(result)
                        listCache[searchValue] = result
                    }
                    isSearching = false
                }
        }

        launch {
            flow {
                while(true) {
                    emit(Unit)
                    delay(30_000)
                }
            }.collectLatest {
                listCache.clear()
            }
        }
    }

    Scaffold(
        topBar = {
            SearchTopBar(
                value = searchValue,
                onValueChange = { searchValue = it },
                onCancel = { focusManager.clearFocus() },
                navController = navController,
            )
        },
        modifier = modifier.imePadding(),
    ) { innerPadding ->
        if (isSearching) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            ) {
                RotatingIcon(
                    painter = painterResource(R.drawable.loading_icon_6),
                    contentDescription = "Searching users",
                    duration = 3000,
                    modifier = Modifier
                        .size(48.dp)
                        .align(alignment = Alignment.Center)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                items(
                    items = userList.toList(),
                    key = { it.docId ?: UUID.randomUUID() }
                ) {
                    SearchUserItem(
                        user = it,
                        onClick = {},
                    )
                }
            }
        }
    }
}

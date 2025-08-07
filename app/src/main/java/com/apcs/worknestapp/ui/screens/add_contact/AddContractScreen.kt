package com.apcs.worknestapp.ui.screens.add_contact

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.apcs.worknestapp.data.remote.user.User
import com.apcs.worknestapp.data.remote.user.UserViewModel
import com.apcs.worknestapp.ui.components.RotatingIcon
import com.apcs.worknestapp.ui.components.topbar.SearchTopBar
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.UUID
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.theme.Inter

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
        snapshotFlow { searchValue }
            .debounce(250)
            .distinctUntilChanged()
            .collectLatest { query ->
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

    Scaffold(
        topBar = {
            SearchTopBar(
                value = searchValue,
                onValueChange = {
                    isSearching = true
                    searchValue = it
                },
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
                    .fillMaxSize()
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

@Composable
fun SearchUserItem(
    user: User,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(color = MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(vertical = 6.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(user.avatar)
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.fade_avatar_fallback),
            error = painterResource(R.drawable.fade_avatar_fallback),
            contentDescription = "User avatar",
            contentScale = ContentScale.Crop,
            filterQuality = FilterQuality.Low,
            modifier = Modifier
                .clip(CircleShape)
                .size(48.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = (user.name ?: "Anonymous"),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 13.sp,
                    lineHeight = 14.sp,
                    modifier = Modifier.weight(1f)
                )

                if (user.pronouns != null && user.pronouns.isNotBlank()) {
                    Text(
                        text = "\u0020\u00b7\u0020" + (user.pronouns),
                        fontWeight = FontWeight.Normal,
                        fontFamily = Inter,
                        fontSize = 11.sp,
                        lineHeight = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Text(
                text = user.email ?: "",
                fontWeight = FontWeight.Normal,
                fontSize = (11.5).sp,
                lineHeight = (11.5).sp,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

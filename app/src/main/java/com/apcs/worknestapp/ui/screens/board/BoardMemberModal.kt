package com.apcs.worknestapp.ui.screens.board

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.board.Board
import com.apcs.worknestapp.data.remote.board.BoardViewModel
import com.apcs.worknestapp.data.remote.user.User
import com.apcs.worknestapp.data.remote.user.UserViewModel
import com.apcs.worknestapp.domain.usecase.AppDefault
import com.apcs.worknestapp.ui.components.CustomSnackBar
import com.apcs.worknestapp.ui.components.inputfield.SearchInput
import com.apcs.worknestapp.ui.theme.Roboto
import com.apcs.worknestapp.ui.theme.success
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun BoardMemberModal(
    board: Board,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    boardViewModel: BoardViewModel,
    userViewModel: UserViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val modalSnackbarHostState = remember { SnackbarHostState() }
    var searchValue by remember { mutableStateOf("") }
    val interactionSource = remember { MutableInteractionSource() }
    val isSearchMode by interactionSource.collectIsFocusedAsState()
    var isSearching by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = {
            it != SheetValue.Hidden
        }
    )
    val members = board.members
    val memberIds = board.memberIds
    val searchUsers = remember { mutableStateListOf<User>() }

    fun clearFocus() {
        focusRequester.requestFocus()
        focusRequester.freeFocus()
        focusManager.clearFocus()
    }

    LaunchedEffect(Unit) {
        launch {
            snapshotFlow { searchValue }
                .debounce(400)
                .distinctUntilChanged()
                .collectLatest { query ->
                    isSearching = true
                    if (query.isBlank()) {
                        searchUsers.clear()
                    } else {
                        val result = userViewModel.findUsers(query)
                        searchUsers.clear()
                        searchUsers.addAll(result)
                    }
                    isSearching = false
                }
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        onDismissRequest = {
            coroutineScope.launch {
                sheetState.hide()
                onDismissRequest()
            }
        },
        modifier = modifier
            .fillMaxSize()
            .padding(WindowInsets.statusBars.asPaddingValues()),
    ) {
        Box(
            modifier = Modifier
                .size(0.dp)
                .focusRequester(focusRequester)
                .focusable()
        )
        Box(modifier = Modifier.fillMaxSize()) {
            SnackbarHost(
                hostState = modalSnackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            ) { CustomSnackBar(data = it) }

            Column(modifier = Modifier.fillMaxSize()) {
                val horizontalPadding = 12.dp

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(vertical = 10.dp)
                ) {
                    AnimatedVisibility(visible = !isSearchMode) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        sheetState.hide()
                                        onDismissRequest()
                                    }
                                },
                                modifier = Modifier.align(alignment = Alignment.CenterStart)
                            ) { Icon(Icons.Default.Close, contentDescription = null) }
                            Text(
                                text = "Manage board members",
                                modifier = Modifier.align(alignment = Alignment.Center)
                            )
                        }
                    }
                    SearchInput(
                        value = searchValue,
                        onValueChange = { searchValue = it },
                        isSearching = isSearching,
                        onCancel = {
                            searchValue = ""
                            clearFocus()
                        },
                        interactionSource = interactionSource,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding, vertical = 4.dp)
                    )
                }

                AnimatedContent(
                    targetState = !isSearchMode
                ) { isSearchMode ->
                    if (isSearchMode) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 24.dp)
                        ) {
                            item(key = "Label") {
                                Text(
                                    text = "Board members (${members.size})",
                                    fontSize = 15.sp,
                                    lineHeight = 15.sp,
                                    fontFamily = Roboto,
                                    modifier = Modifier.padding(horizontal = horizontalPadding)
                                )
                            }

                            item { Spacer(modifier = Modifier.height(16.dp)) }

                            items(
                                items = members,
                                key = { it.docId ?: UUID.randomUUID() }
                            ) { user ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable(onClick = {
                                            if (user.docId != board.ownerId) {

                                            }
                                        })
                                        .fillMaxWidth()
                                        .padding(horizontal = horizontalPadding, vertical = 10.dp)
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context).data(user.avatar)
                                            .crossfade(true).build(),
                                        placeholder = painterResource(R.drawable.fade_avatar_fallback),
                                        error = painterResource(R.drawable.fade_avatar_fallback),
                                        contentDescription = "Avatar",
                                        contentScale = ContentScale.Crop,
                                        filterQuality = FilterQuality.Medium,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .aspectRatio(1f)
                                            .clip(CircleShape),
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    ) {
                                        Text(
                                            text = user.name ?: AppDefault.USER_NAME,
                                            fontSize = 15.sp,
                                            lineHeight = 15.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = user.email ?: "",
                                            fontSize = 12.sp,
                                            lineHeight = 12.sp,
                                            fontWeight = FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (user.docId == board.ownerId) "Owner"
                                        else "Member",
                                        fontSize = 12.sp,
                                        lineHeight = 12.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 24.dp)
                        ) {
                            items(
                                items = searchUsers,
                                key = { it.docId ?: UUID.randomUUID() }
                            ) { user ->
                                val isMember = memberIds.any { it == user.docId }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable(onClick = {
                                            val boardId = board.docId
                                            if (!isMember && boardId != null) {
                                                coroutineScope.launch {
                                                    val message =
                                                        boardViewModel.addMemberToBoard(
                                                            boardId,
                                                            user
                                                        )
                                                    if (message != null) {
                                                        modalSnackbarHostState.showSnackbar(
                                                            message = message,
                                                            withDismissAction = true,
                                                        )
                                                    } else {
                                                        modalSnackbarHostState.showSnackbar(
                                                            message = "Add user ${user.name} successfully",
                                                            withDismissAction = true,
                                                        )
                                                    }
                                                }
                                            }
                                        })
                                        .fillMaxWidth()
                                        .padding(horizontal = horizontalPadding, vertical = 10.dp)
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context).data(user.avatar)
                                            .crossfade(true).build(),
                                        placeholder = painterResource(R.drawable.fade_avatar_fallback),
                                        error = painterResource(R.drawable.fade_avatar_fallback),
                                        contentDescription = "Avatar",
                                        contentScale = ContentScale.Crop,
                                        filterQuality = FilterQuality.Medium,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .aspectRatio(1f)
                                            .clip(CircleShape),
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    ) {
                                        Text(
                                            text = user.name ?: AppDefault.USER_NAME,
                                            fontSize = 15.sp,
                                            lineHeight = 15.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = user.email ?: "",
                                            fontSize = 12.sp,
                                            lineHeight = 12.sp,
                                            fontWeight = FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (isMember) {
                                        Icon(
                                            painter = painterResource(R.drawable.outline_circle_checkmark),
                                            contentDescription = "Is a member",
                                            tint = MaterialTheme.colorScheme.success,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else {
                                        Icon(
                                            painter = painterResource(R.drawable.outline_circle_add),
                                            contentDescription = "Add member",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

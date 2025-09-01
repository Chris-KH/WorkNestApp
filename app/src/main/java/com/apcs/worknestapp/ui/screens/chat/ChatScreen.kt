package com.apcs.worknestapp.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.message.Message
import com.apcs.worknestapp.data.remote.message.MessageType
import com.apcs.worknestapp.data.remote.message.MessageViewModel
import com.apcs.worknestapp.domain.usecase.AppDefault
import com.apcs.worknestapp.ui.components.ChatInputSection
import com.apcs.worknestapp.ui.components.topbar.TopBarDefault
import com.apcs.worknestapp.ui.theme.Roboto
import com.apcs.worknestapp.ui.theme.success
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conservationId: String,
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
    messageViewModel: MessageViewModel = hiltViewModel(),
) {
    val authId = FirebaseAuth.getInstance().currentUser?.uid

    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    val conservation = messageViewModel.currentConservation.collectAsState()
    var chatFocused by remember { mutableStateOf(false) }
    var textMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        messageViewModel.getConservation(docId = conservationId)
        messageViewModel.registerMessageListener(conservationId)
        messageViewModel.updateConservationSeen(conservationId, true)
    }

    DisposableEffect(Unit) {
        onDispose {
            messageViewModel.getConservation(docId = null)
        }
    }

    LifecycleResumeEffect(Unit) {
        messageViewModel.getConservation(docId = conservationId)
        messageViewModel.registerMessageListener(conservationId)

        onPauseOrDispose {
            messageViewModel.removeMessageListener(conservationId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.wrapContentSize()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(conservation.value?.userData?.avatar ?: AppDefault.AVATAR)
                                    .crossfade(true)
                                    .build(),
                                placeholder = painterResource(R.drawable.fade_avatar_fallback),
                                error = painterResource(R.drawable.fade_avatar_fallback),
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                filterQuality = FilterQuality.Low,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                            )
                            if (conservation.value?.userData?.online == true) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(MaterialTheme.colorScheme.success, CircleShape)
                                        .align(alignment = Alignment.BottomEnd)
                                        .zIndex(10f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = conservation.value?.userData?.name ?: AppDefault.USER_NAME,
                                fontSize = 15.sp,
                                lineHeight = 15.sp,
                                fontFamily = Roboto,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = if (conservation.value?.userData?.online == true) "Online"
                                else "Offline",
                                fontSize = 12.sp,
                                lineHeight = 12.sp,
                                fontFamily = Roboto,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.symbol_angle_arrow),
                            contentDescription = "Go back",
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(90f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            painter = painterResource(R.drawable.outline_menu_3dot),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                expandedHeight = TopBarDefault.expandedHeight,
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier.clickable(
                    onClick = { },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
            )
        },
        modifier = modifier.clickable(
            onClick = { focusManager.clearFocus() },
            indication = null, interactionSource = remember { MutableInteractionSource() }
        ),
    ) { innerPadding ->
        if (conservation.value != null) {
            val listState = rememberLazyListState()
            val messages = conservation.value!!.messages

            LaunchedEffect(messages.size) {
                if (messages.firstOrNull()?.sender?.id == authId)
                    listState.animateScrollToItem(0)
            }

            Column(
                modifier = Modifier
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                        end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                    )
                    .imePadding()
                    .fillMaxSize()
            ) {
                LazyColumn(
                    state = listState,
                    reverseLayout = true,
                    verticalArrangement = Arrangement.Top,
                    contentPadding = PaddingValues(vertical = 12.dp, horizontal = 10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    itemsIndexed(
                        items = messages,
                        key = { _, it -> it.docId ?: UUID.randomUUID() }
                    ) { idx, mes ->
                        MessageItem(
                            message = mes,
                            conservation = conservation.value!!,
                            isMyMessage = mes.sender?.id == authId,
                            isFirstMessage = idx + 1 == messages.size,
                            isLastMessage = idx == 0,
                            modifier = Modifier,
                        )
                        if (idx + 1 < messages.size) {
                            if (mes.sender?.id != messages[idx + 1].sender?.id) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }

                    item(key = "spacer-0") { Spacer(modifier = Modifier.height(16.dp)) }
                }
                ChatInputSection(
                    text = textMessage,
                    onTextChange = { textMessage = it },
                    onSend = {
                        if (textMessage.isNotBlank()) {
                            val message = Message(
                                content = textMessage,
                                type = MessageType.TEXT.name
                            )
                            textMessage = ""
                            messageViewModel.sendMessage(conservationId, message)
                        }
                    },
                    modifier = Modifier
                        .onFocusChanged { chatFocused = it.isFocused }
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .let {
                            if (chatFocused) return@let it
                            return@let it.padding(bottom = innerPadding.calculateBottomPadding())
                        }
                )
            }
        }
    }
}

package com.apcs.worknestapp.ui.screens.board

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.board.Board
import com.apcs.worknestapp.data.remote.board.BoardViewModel
import com.apcs.worknestapp.ui.components.ConfirmDialog
import com.apcs.worknestapp.ui.components.ConfirmDialogState
import com.apcs.worknestapp.ui.components.CoverPickerModal
import com.apcs.worknestapp.ui.components.CustomSnackBar
import com.apcs.worknestapp.ui.components.RotatingIcon
import com.apcs.worknestapp.ui.theme.Roboto
import com.apcs.worknestapp.utils.ColorUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

enum class BoardSubModal {
    MEMBERS,
    BACKGROUND,
    INFO,
    ARCHIVE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardModal(
    board: Board,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    navController: NavHostController,
    boardViewModel: BoardViewModel,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val modalSnackbarHostState = remember { SnackbarHostState() }
    var showSubModal by rememberSaveable { mutableStateOf<BoardSubModal?>(null) }
    var showConfirmDialog by remember { mutableStateOf<ConfirmDialogState?>(null) }
    var isSyncing by remember { mutableStateOf(false) }
    var isBoardMenu by remember { mutableStateOf(true) }
    var showNoteCover by remember(board.showNoteCover) {
        mutableStateOf(board.showNoteCover ?: false)
    }
    var showCompletedStatus by remember(board.showCompletedStatus) {
        mutableStateOf(board.showCompletedStatus ?: false)
    }
    var backgroundColor by remember(board.cover) {
        mutableStateOf(board.cover?.let { ColorUtils.safeParse(it) })
    }
    val listItemTextStyle = TextStyle(
        fontSize = 15.sp, lineHeight = 16.sp,
        fontWeight = FontWeight.Medium, fontFamily = Roboto,
    )

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
            .fillMaxSize()
            .padding(WindowInsets.statusBars.asPaddingValues())

    ) {
        when(showSubModal) {
            BoardSubModal.MEMBERS -> {
                BoardMemberModal(
                    board = board,
                    onDismissRequest = { showSubModal = null },
                    boardViewModel = boardViewModel,
                )
            }

            BoardSubModal.BACKGROUND -> {
                CoverPickerModal(
                    currentColor = backgroundColor,
                    onDismissRequest = { showSubModal = null },
                    onSave = { newColor ->
                        showSubModal = null
                        val boardId = board.docId
                        if (boardId != null && backgroundColor != newColor) {
                            coroutineScope.launch {
                                val prevState = backgroundColor
                                backgroundColor = newColor
                                val message =
                                    boardViewModel.updateBoardCover(boardId, newColor?.toArgb())
                                if (message != null) {
                                    backgroundColor = prevState
                                    modalSnackbarHostState.showSnackbar(
                                        message = message,
                                        withDismissAction = true,
                                    )
                                }
                            }
                        }
                    }
                )
            }

            BoardSubModal.INFO -> {
                BoardInfoModal(
                    board = board,
                    onDismissRequest = { showSubModal = null },
                    boardViewModel = boardViewModel,
                )
            }

            BoardSubModal.ARCHIVE -> {
                BoardArchiveModal(
                    board = board,
                    onDismissRequest = { showSubModal = null },
                    navController = navController,
                    boardViewModel = boardViewModel,
                )
            }

            else -> {}
        }

        showConfirmDialog?.let {
            ConfirmDialog(
                title = it.title,
                message = it.message,
                onDismissRequest = { showConfirmDialog = null },
                confirmText = it.confirmText,
                cancelText = it.cancelText,
                onConfirm = it.onConfirm,
                onCancel = it.onCancel,
            )
        }
        Box(modifier = Modifier.fillMaxSize()) {
            SnackbarHost(
                hostState = modalSnackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(1000f)
            ) { CustomSnackBar(data = it) }

            Column(modifier = Modifier.fillMaxSize()) {
                val animationDuration = 400

                AnimatedContent(
                    targetState = isBoardMenu,
                    transitionSpec = {
                        if (initialState && !targetState) {
                            (slideInHorizontally(animationSpec = tween(animationDuration)) { it } + fadeIn(
                                animationSpec = tween(animationDuration)
                            ))
                                .togetherWith(
                                    slideOutHorizontally(animationSpec = tween(animationDuration)) { -it }
                                            + fadeOut(animationSpec = tween(animationDuration))
                                )
                        } else {
                            (slideInHorizontally(animationSpec = tween(animationDuration)) { -it } + fadeIn(
                                animationSpec = tween(animationDuration)
                            ))
                                .togetherWith(
                                    slideOutHorizontally(animationSpec = tween(animationDuration)) { it }
                                            + fadeOut(animationSpec = tween(animationDuration))
                                )
                        }.using(SizeTransform(clip = false))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                )
                { isMenu ->
                    Box(
                        modifier = Modifier.padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isMenu) {
                            IconButton(
                                onClick = onDismissRequest,
                                modifier = Modifier.align(alignment = Alignment.CenterStart),
                            ) { Icon(Icons.Default.Close, contentDescription = null) }
                            Text(
                                text = "Board Menu",
                                modifier = Modifier.align(alignment = Alignment.Center)
                            )
                            IconButton(
                                onClick = { isBoardMenu = false },
                                modifier = Modifier.align(alignment = Alignment.CenterEnd)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .rotate(90f)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { isBoardMenu = true },
                                modifier = Modifier.align(alignment = Alignment.CenterStart)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null
                                )
                            }
                            Text(
                                text = "Board Settings",
                                modifier = Modifier.align(alignment = Alignment.Center)
                            )
                        }
                    }

                }

                AnimatedContent(
                    targetState = isBoardMenu,
                    transitionSpec = {
                        if (initialState && !targetState) {
                            (slideInHorizontally(animationSpec = tween(animationDuration)) { it } + fadeIn(
                                animationSpec = tween(animationDuration)
                            ))
                                .togetherWith(
                                    slideOutHorizontally(animationSpec = tween(animationDuration)) { -it }
                                            + fadeOut(animationSpec = tween(animationDuration))
                                )
                        } else {
                            (slideInHorizontally(animationSpec = tween(animationDuration)) { -it } + fadeIn(
                                animationSpec = tween(animationDuration)
                            ))
                                .togetherWith(
                                    slideOutHorizontally(animationSpec = tween(animationDuration)) { it }
                                            + fadeOut(animationSpec = tween(animationDuration))
                                )
                        }.using(SizeTransform(clip = false))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { isMenu ->
                    if (isMenu) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentPadding = PaddingValues(vertical = 16.dp)
                            ) {
                                item(key = "Members") {
                                    ListItem(
                                        headlineContent = {
                                            Text(text = "Members", style = listItemTextStyle)
                                        },
                                        leadingContent = {
                                            Icon(
                                                painter = painterResource(R.drawable.outline_members),
                                                contentDescription = "Members",
                                                modifier = Modifier.size(24.dp)
                                            )
                                        },
                                        trailingContent = {
                                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                                        },
                                        modifier = Modifier.clickable(
                                            onClick = { showSubModal = BoardSubModal.MEMBERS }
                                        )
                                    )
                                }
                                item(key = "Change background") {
                                    ListItem(
                                        headlineContent = {
                                            Text(
                                                text = "Change background",
                                                style = listItemTextStyle
                                            )
                                        },
                                        leadingContent = {
                                            Icon(
                                                painter = painterResource(R.drawable.outline_palette),
                                                contentDescription = "Change background",
                                                modifier = Modifier.size(24.dp)
                                            )
                                        },
                                        trailingContent = {
                                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                                        },
                                        modifier = Modifier.clickable(
                                            onClick = { showSubModal = BoardSubModal.BACKGROUND }
                                        )
                                    )
                                }

                                item { Spacer(modifier = Modifier.height(16.dp)) }
                                item(key = "BoardInfo") {
                                    ListItem(
                                        headlineContent = {
                                            Text(
                                                text = "About this board",
                                                style = listItemTextStyle
                                            )
                                        },
                                        leadingContent = {
                                            Icon(
                                                painter = painterResource(R.drawable.outline_info),
                                                contentDescription = "Board information",
                                                modifier = Modifier.size(24.dp)
                                            )
                                        },
                                        trailingContent = {
                                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                                        },
                                        modifier = Modifier.clickable(
                                            onClick = { showSubModal = BoardSubModal.INFO }
                                        )
                                    )
                                }

                                item { Spacer(modifier = Modifier.height(16.dp)) }
                                item(key = "Show note covers") {
                                    ListItem(
                                        headlineContent = {
                                            Text(
                                                text = "Show note covers",
                                                style = listItemTextStyle
                                            )
                                        },
                                        leadingContent = {
                                            Icon(
                                                painter = painterResource(R.drawable.outline_background),
                                                contentDescription = "Show note covers",
                                                modifier = Modifier.size(24.dp)
                                            )
                                        },
                                        trailingContent = {
                                            Switch(
                                                checked = showNoteCover,
                                                onCheckedChange = {
                                                    val boardId = board.docId
                                                    if (boardId != null) {
                                                        coroutineScope.launch {
                                                            val prevState = showNoteCover
                                                            showNoteCover = !prevState
                                                            val message = boardViewModel
                                                                .updateBoardShowNoteCover(
                                                                    boardId, !prevState
                                                                )
                                                            if (message != null) {
                                                                showNoteCover = prevState
                                                                modalSnackbarHostState.showSnackbar(
                                                                    message = message,
                                                                    withDismissAction = true,
                                                                )
                                                            }
                                                        }
                                                    }
                                                },
                                            )
                                        },
                                    )
                                }
                                item(key = "Show completed status") {
                                    ListItem(
                                        headlineContent = {
                                            Text(
                                                text = "Show completed status",
                                                style = listItemTextStyle,
                                            )
                                        },
                                        leadingContent = {
                                            Icon(
                                                painter = painterResource(R.drawable.outline_completed_status),
                                                contentDescription = "Show completed status",
                                                modifier = Modifier.size(24.dp)
                                            )
                                        },
                                        trailingContent = {
                                            Switch(
                                                checked = showCompletedStatus,
                                                onCheckedChange = {
                                                    val boardId = board.docId
                                                    if (boardId != null) {
                                                        coroutineScope.launch {
                                                            val prevState = showCompletedStatus
                                                            showCompletedStatus = !prevState
                                                            val message = boardViewModel
                                                                .updateBoardShowCompletedStatus(
                                                                    boardId, !prevState
                                                                )
                                                            if (message != null) {
                                                                showCompletedStatus = prevState
                                                                modalSnackbarHostState.showSnackbar(
                                                                    message = message,
                                                                    withDismissAction = true,
                                                                )
                                                            }
                                                        }
                                                    }
                                                },
                                            )
                                        },
                                    )
                                }

                                item { Spacer(modifier = Modifier.height(16.dp)) }
                                item(key = "Archive") {
                                    ListItem(
                                        headlineContent = {
                                            Text(
                                                text = "Archive",
                                                style = listItemTextStyle
                                            )
                                        },
                                        leadingContent = {
                                            Icon(
                                                painter = painterResource(R.drawable.outline_archive),
                                                contentDescription = "Archive",
                                                modifier = Modifier.size(24.dp)
                                            )
                                        },
                                        trailingContent = {
                                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                                        },
                                        modifier = Modifier.clickable(
                                            onClick = { showSubModal = BoardSubModal.ARCHIVE }
                                        )
                                    )
                                }
                                item(key = "Archive completed notes") {
                                    ListItem(
                                        headlineContent = {
                                            Text(
                                                text = "Archive completed notes",
                                                style = listItemTextStyle
                                            )
                                        },
                                        leadingContent = {
                                            Icon(
                                                painter = painterResource(R.drawable.outline_store),
                                                contentDescription = "Archive completed notes",
                                                modifier = Modifier.size(24.dp)
                                            )
                                        },
                                        modifier = Modifier.clickable(onClick = {})
                                    )
                                }

                                item { Spacer(modifier = Modifier.height(32.dp)) }
                                item(key = "Delete board") {
                                    ListItem(
                                        headlineContent = {
                                            Text(text = "Delete board", style = listItemTextStyle)
                                        },
                                        leadingContent = {
                                            Icon(
                                                painter = painterResource(R.drawable.outline_trash),
                                                contentDescription = "Delete board",
                                                modifier = Modifier.size(24.dp)
                                            )
                                        },
                                        colors = ListItemDefaults.colors(
                                            headlineColor = MaterialTheme.colorScheme.error,
                                            leadingIconColor = MaterialTheme.colorScheme.error,
                                        ),
                                        modifier = Modifier.clickable(
                                            onClick = {
                                                showConfirmDialog = ConfirmDialogState(
                                                    title = "Delete this board",
                                                    message = "Deleted board cannot be recovered",
                                                    confirmText = "Delete",
                                                    cancelText = "Cancel",
                                                    onConfirm = {
                                                        showConfirmDialog = null
                                                        val boardId = board.docId
                                                        if (boardId != null) {
                                                            coroutineScope.launch {
                                                                val message =
                                                                    boardViewModel.deleteBoard(
                                                                        boardId
                                                                    )
                                                                if (message != null) {
                                                                    modalSnackbarHostState.showSnackbar(
                                                                        message = message,
                                                                        withDismissAction = true,
                                                                    )
                                                                } else {
                                                                    modalSnackbarHostState.showSnackbar(
                                                                        message = "Board is deleted",
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    },
                                                    onCancel = { showConfirmDialog = null }
                                                )
                                            }
                                        )
                                    )
                                }

                                item { Spacer(modifier = Modifier.height(160.dp)) }
                            }
                            HorizontalDivider(thickness = 1.dp)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable(onClick = {
                                        val boardId = board.docId
                                        if (!isSyncing && boardId != null) {
                                            coroutineScope.launch {
                                                isSyncing = true
                                                joinAll(
                                                    launch { boardViewModel.getBoard(boardId) },
                                                    launch { delay(1000) }
                                                )
                                                isSyncing = false
                                            }
                                        }
                                    })
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 20.dp),
                            ) {
                                if (isSyncing) {
                                    RotatingIcon(
                                        painter = painterResource(R.drawable.loading_icon_7),
                                        contentDescription = "Syncing",
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(R.drawable.loading_icon_7),
                                        contentDescription = "Synced",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(20.dp))
                                Text(
                                    text = if (isSyncing) "Syncing..."
                                    else "Synced",
                                    fontWeight = FontWeight.Normal
                                )
                            }
                            HorizontalDivider(thickness = 1.dp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {

                        }
                    }
                }
            }
        }
    }
}

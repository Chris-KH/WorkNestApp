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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.board.Board
import com.apcs.worknestapp.data.remote.board.BoardViewModel
import com.apcs.worknestapp.ui.components.RotatingIcon
import com.apcs.worknestapp.ui.theme.Roboto
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardSettingModal(
    board: Board,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
    boardViewModel: BoardViewModel,
) {
    val coroutineScope = rememberCoroutineScope()
    var isBoardMenu by remember { mutableStateOf(true) }
    var showNoteCover by remember(board.showNoteCover) {
        mutableStateOf(board.showNoteCover ?: false)
    }
    var showCompletedStatus by remember(board.showCompletedStatus) {
        mutableStateOf(board.showCompletedStatus ?: false)
    }
    val listItemTextStyle = TextStyle(
        fontSize = 15.sp, lineHeight = 16.sp,
        fontWeight = FontWeight.Medium, fontFamily = Roboto,
    )

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        dragHandle = null,
        shape = RoundedCornerShape(0.dp),
        modifier = modifier
            .padding(WindowInsets.statusBars.asPaddingValues())
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
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
                    modifier = Modifier.padding(vertical = 4.dp),
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
                        ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
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
                                    modifier = Modifier.clickable(onClick = {})
                                )
                            }
                            item(key = "Change background") {
                                ListItem(
                                    headlineContent = {
                                        Text(text = "Change background", style = listItemTextStyle)
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
                                    modifier = Modifier.clickable(onClick = {})
                                )
                            }

                            item { Spacer(modifier = Modifier.height(16.dp)) }
                            item(key = "BoardInfo") {
                                ListItem(
                                    headlineContent = {
                                        Text(text = "About this board", style = listItemTextStyle)
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
                                    modifier = Modifier.clickable(onClick = {})
                                )
                            }

                            item { Spacer(modifier = Modifier.height(16.dp)) }
                            item(key = "Show note covers") {
                                ListItem(
                                    headlineContent = {
                                        Text(text = "Show note covers", style = listItemTextStyle)
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
                                                            snackbarHost.showSnackbar(
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
                                                            snackbarHost.showSnackbar(
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
                                    modifier = Modifier.clickable(onClick = {})
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
                                    modifier = Modifier.clickable(onClick = {})
                                )
                            }

                            item { Spacer(modifier = Modifier.height(160.dp)) }
                        }
                        HorizontalDivider(thickness = 1.dp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 20.dp),
                        ) {
                            RotatingIcon(
                                painter = painterResource(R.drawable.loading_icon_7),
                                contentDescription = "Syncing",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            Text(text = "Sync", fontWeight = FontWeight.Normal)
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

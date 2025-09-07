package com.apcs.worknestapp.ui.screens.board

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavHostController
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.board.BoardViewModel
import com.apcs.worknestapp.data.remote.board.NoteList
import com.apcs.worknestapp.ui.components.LoadingScreen
import com.apcs.worknestapp.ui.components.inputfield.CustomTextField
import com.apcs.worknestapp.ui.components.topbar.TopBarDefault
import com.apcs.worknestapp.ui.theme.Roboto
import com.apcs.worknestapp.utils.ColorUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardScreen(
    boardId: String,
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
    boardViewModel: BoardViewModel = hiltViewModel(),
) {
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    var isFirstLoad by remember { mutableStateOf(true) }
    val topAppBarColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
    val topAppBarContent = MaterialTheme.colorScheme.onSurface
    var isZoomIn by rememberSaveable { mutableStateOf(false) }
    var showSettingModal by rememberSaveable { mutableStateOf(false) }
    val settingModalState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { false }
    )

    val board = boardViewModel.currentBoard.collectAsState()
    val boardCoverColor = board.value?.cover?.let { ColorUtils.safeParse(it) }
    var editableBoardName by remember(board.value?.name) {
        val name = board.value?.name ?: ""
        mutableStateOf(
            TextFieldValue(
                text = name,
                selection = TextRange(name.length)
            )
        )
    }

    LaunchedEffect(Unit) {
        isFirstLoad = true
        val remoteBoard = boardViewModel.getBoard(boardId)
        if (remoteBoard == null) {
            navController.popBackStack()
            snackbarHost.showSnackbar(
                message = "Load board failed. Board not founded",
                withDismissAction = true,
            )
        }
        isFirstLoad = false
    }

    LaunchedEffect(board.value) {
        if (board.value == null && !isFirstLoad) {
            settingModalState.hide()
            showSettingModal = false
            delay(1000)
            navController.popBackStack()
        }
    }

    LifecycleResumeEffect(boardId) {
        boardViewModel.registerNoteListListener(boardId)
        board.value?.noteLists?.forEach { noteList ->
            noteList.docId?.let { boardViewModel.registerNoteListener(boardId, it) }
        }
        onPauseOrDispose {
            boardViewModel.removeNoteListener()
            boardViewModel.removeNoteListListener()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (!isFirstLoad && board.value != null) {
                        CustomTextField(
                            value = editableBoardName,
                            onValueChange = { editableBoardName = it },
                            textStyle = TextStyle(
                                fontWeight = FontWeight.Normal,
                                fontFamily = Roboto,
                                fontSize = 16.sp,
                                lineHeight = 16.sp,
                                letterSpacing = (0).sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            containerColor = Color.Transparent,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    coroutineScope.launch {
                                        val initialName = board.value?.name ?: ""
                                        val newName = editableBoardName.text
                                        if (newName.isNotBlank() && newName != initialName) {
                                            val message = boardViewModel.updateBoardName(
                                                boardId, newName
                                            )
                                            if (message != null) {
                                                focusManager.clearFocus()
                                                editableBoardName = TextFieldValue(
                                                    text = initialName,
                                                    selection = TextRange(initialName.length)
                                                )
                                                snackbarHost.showSnackbar(
                                                    message = message,
                                                    withDismissAction = true,
                                                )
                                            } else focusManager.clearFocus()
                                        } else focusManager.clearFocus()
                                    }
                                }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 8.dp),
                        )
                    } else {
                        Text(
                            text = "Loading Board...",
                            fontSize = 16.sp,
                            lineHeight = 16.sp,
                            fontFamily = Roboto,
                            letterSpacing = (0.25).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                showSettingModal = true
                                settingModalState.show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Board modal",
                            modifier = Modifier
                                .size(28.dp)
                                .rotate(90f)
                        )
                    }
                },
                expandedHeight = TopBarDefault.expandedHeight,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = topAppBarColor,
                    scrolledContainerColor = topAppBarColor,
                    titleContentColor = topAppBarContent,
                    actionIconContentColor = topAppBarContent,
                    navigationIconContentColor = topAppBarContent,
                )
            )
        },
        floatingActionButton = {
            Button(
                onClick = { isZoomIn = !isZoomIn },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(
                        if (isZoomIn) R.drawable.outline_zoom_out
                        else R.drawable.outline_zoom_in
                    ),
                    contentDescription = "Zoom", modifier = Modifier.size(24.dp)
                )
            }
        },
        containerColor = if (!isFirstLoad && board.value != null) boardCoverColor ?: Color.Gray
        else MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        if (isFirstLoad || board.value == null) {
            LoadingScreen(modifier = Modifier.padding(innerPadding))
        } else {
            val listState = rememberLazyListState()
            val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
            val windowInfo = LocalWindowInfo.current
            val screenWidth = with(LocalDensity.current) {
                windowInfo.containerSize.width.toDp()
            }
            val scale by animateFloatAsState(
                targetValue = if (isZoomIn) 1f else 0.5f, label = "cardScale"
            )
            val horizontalPadding = 16.dp
            val cardWith = (screenWidth - horizontalPadding * 2)

            LaunchedEffect(isZoomIn) {
                if (isZoomIn) {
                    val layout = listState.layoutInfo
                    val visibleItems = layout.visibleItemsInfo
                    if (visibleItems.isNotEmpty()) {
                        val viewportCenter =
                            layout.viewportStartOffset + layout.viewportSize.width / 2
                        val nearest = visibleItems.minByOrNull {
                            kotlin.math.abs(it.offset + it.size - viewportCenter)
                        }
                        nearest?.let { listState.animateScrollToItem(it.index, 0) }
                    }
                }
            }

            if (showSettingModal) {
                BoardSettingModal(
                    board = board.value!!,
                    sheetState = settingModalState,
                    snackbarHost = snackbarHost,
                    onDismissRequest = {
                        coroutineScope.launch {
                            settingModalState.hide()
                            showSettingModal = false
                        }
                    },
                    boardViewModel = boardViewModel,
                )
            }

            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .clickable(
                        onClick = { focusManager.clearFocus() },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .fillMaxSize()
                    .layout { measurable, constraints ->
                        val s = scale.coerceAtLeast(0.0001f)

                        if (s == 1f) {
                            val placeable = measurable.measure(constraints)
                            layout(placeable.width, placeable.height) {
                                placeable.placeRelative(0, 0)
                            }
                        } else {
                            fun Int.divByScale(): Int {
                                val v = this.toDouble() / s
                                return if (v > Int.MAX_VALUE) Int.MAX_VALUE else v.roundToInt()
                            }

                            val scaledMinW = constraints.minWidth.divByScale().coerceAtLeast(0)
                            val scaledMaxW =
                                constraints.maxWidth.divByScale().coerceAtLeast(scaledMinW)
                            val scaledMinH = constraints.minHeight.divByScale().coerceAtLeast(0)
                            val scaledMaxH =
                                constraints.maxHeight.divByScale().coerceAtLeast(scaledMinH)

                            val scaledConstraints = Constraints(
                                minWidth = scaledMinW,
                                maxWidth = scaledMaxW,
                                minHeight = scaledMinH,
                                maxHeight = scaledMaxH
                            )

                            val placeable = measurable.measure(scaledConstraints)
                            val finalWidth = (placeable.width * s).roundToInt()
                            val finalHeight = (placeable.height * s).roundToInt()
                            val width =
                                finalWidth.coerceIn(constraints.minWidth, constraints.maxWidth)
                            val height =
                                finalHeight.coerceIn(constraints.minHeight, constraints.maxHeight)

                            layout(width, height) {
                                val dx = (width - finalWidth) / 2
                                val dy = (height - finalHeight) / 2

                                placeable.placeRelativeWithLayer(dx, dy) {
                                    scaleX = s
                                    scaleY = s
                                    transformOrigin = TransformOrigin(0f, 0f)
                                }
                            }
                        }
                    }
            ) {
                LazyRow(
                    state = listState,
                    flingBehavior = if (isZoomIn) flingBehavior
                    else ScrollableDefaults.flingBehavior(),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = horizontalPadding,
                        vertical = 12.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(horizontalPadding),
                ) {
                    items(
                        items = board.value?.noteLists ?: emptyList(),
                        key = { it.docId ?: UUID.randomUUID() }) { noteList ->
                        NoteListCard(
                            boardId = boardId,
                            noteList = noteList,
                            board = board.value!!,
                            boardViewModel = boardViewModel,
                            onUpdateNoteListName = { newName ->
                                val noteListId = noteList.docId
                                if (noteListId != null) {
                                    coroutineScope.launch {
                                        val message = boardViewModel.updateNoteListName(
                                            boardId, noteListId, newName
                                        )
                                        if (message != null) {
                                            snackbarHost.showSnackbar(
                                                message = message,
                                                withDismissAction = true,
                                            )
                                        }
                                    }
                                }
                            },
                            onRemoveNoteList = {
                                val noteListId = noteList.docId
                                if (noteListId != null) {
                                    coroutineScope.launch {
                                        val message =
                                            boardViewModel.removeNoteList(boardId, noteListId)
                                        if (message != null) {
                                            snackbarHost.showSnackbar(
                                                message = message,
                                                withDismissAction = true,
                                            )
                                        }
                                    }
                                }
                            },
                            snackbarHost = snackbarHost,
                            navController = navController,
                            modifier = Modifier.width(cardWith),
                        )
                    }
                    item(key = "Add note list button") {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    boardViewModel.addNoteList(boardId, NoteList())
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = topAppBarColor,
                                contentColor = topAppBarContent,
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 0.dp, horizontal = 20.dp),
                            modifier = Modifier.width(cardWith)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add Note List",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Add new list",
                                fontSize = 14.sp,
                                lineHeight = 14.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

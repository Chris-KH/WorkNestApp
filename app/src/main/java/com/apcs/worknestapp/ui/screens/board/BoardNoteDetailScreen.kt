package com.apcs.worknestapp.ui.screens.board

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.apcs.worknestapp.data.remote.board.BoardViewModel

enum class NoteModalBottomType {
    COVER,
    DESCRIPTION,
    START_DATE,
    END_DATE,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardNoteDetailScreen(
    boardId: String,
    noteListId: String,
    noteId: String,
    snackbarHost: SnackbarHostState,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    boardViewModel: BoardViewModel = hiltViewModel(),
) {
}
/*{
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    var isFirstLoading by remember { mutableStateOf(true) }
    var modalBottomType by remember { mutableStateOf<NoteModalBottomType?>(null) }

    val note by boardViewModel.selectedNote.collectAsState()

    var noteName by remember(note?.name) { mutableStateOf(note?.name ?: "") }
    val noteCoverColor = note?.cover?.let { ColorUtils.safeParse(it) }

    boardViewModel.getChecklists(boardId, noteListId, noteId)
    val checklists by boardViewModel.checklists.collectAsState()

    var commentInputFocused by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    var commentList by remember { mutableStateOf(emptyList<Comment>()) }

    //LayoutState
    val lazyListState = rememberLazyListState()
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceColorOverlay = surfaceColor.copy(alpha = 0.04f)
    var topBarBackground by remember { mutableStateOf(surfaceColorOverlay) }

    LaunchedEffect(Unit) {
        Log.d("BoardNoteDetailScreen", "BoardNoteDetailScreen is being launched.")
        isFirstLoading = true
        boardViewModel.getNote(boardId, noteListId, noteId)
        isFirstLoading = false
    }
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.firstVisibleItemIndex }.collect {
            topBarBackground = if (it != 0) surfaceColor
            else surfaceColorOverlay
        }
    }
    DisposableEffect(key1 = Unit) {
        onDispose {
            Log.d("BoardNoteDetailScreen", "BoardNoteDetailScreen is being disposed.")
        }
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                field = if (topBarBackground == surfaceColorOverlay) ""
                else (note?.name ?: ""),
                showDivider = false,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarBackground,
                    scrolledContainerColor = topBarBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                actions = {
                    var showDropdownMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showDropdownMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            modifier = Modifier
                                .size(28.dp)
                                .rotate(-90f)
                        )

                        DropdownMenu(
                            expanded = showDropdownMenu,
                            onDismissRequest = { showDropdownMenu = false },
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.widthIn(min = 200.dp)
                        ) {
                            val dropdownTextStyle = TextStyle(
                                fontSize = 15.sp, lineHeight = 16.sp,
                                fontFamily = Roboto, fontWeight = FontWeight.Normal,
                            )
                            val horizontalPadding = 20.dp
                            val iconSize = 24.dp

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (note?.archived == true) "Restore"
                                        else "Archive",
                                        style = dropdownTextStyle
                                    )
                                },
                                onClick = {
                                    coroutineScope.launch {
                                        showDropdownMenu = false
                                        val isArchived = note?.archived == true
                                        val isSuccess = boardViewModel.updateNoteArchive(
                                            boardId = boardId,
                                            noteListId = noteListId,
                                            docId = noteId,
                                            newState = !isArchived
                                        )
                                        if (!isSuccess) {
                                            snackbarHost.showSnackbar(
                                                message = "${
                                                    if (isArchived) "Restore"
                                                    else "Archive"
                                                } note completed failed",
                                                withDismissAction = true,
                                            )
                                        } else {
                                            snackbarHost.showSnackbar(
                                                message = "The note was ${
                                                    if (isArchived) "unarchived"
                                                    else "archived"
                                                }",
                                                withDismissAction = true,
                                            )
                                        }
                                    }
                                },
                                trailingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.outline_archive),
                                        contentDescription = "Archive note",
                                        modifier = Modifier.size(iconSize),
                                    )
                                },
                                contentPadding = PaddingValues(horizontal = horizontalPadding)
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Text(text = "Delete", style = dropdownTextStyle)
                                },
                                onClick = {
                                    showDropdownMenu = false
                                    val isSuccess = boardViewModel.removeNoteFromNoteList(
                                        boardId,
                                        noteListId,
                                        noteId
                                    )

                                    coroutineScope.launch {
                                        if (!isSuccess) snackbarHost.showSnackbar(
                                            message = "Delete note ${note?.name ?: ""} failed",
                                            withDismissAction = true,
                                        )
                                    }
                                },
                                trailingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.outline_trash),
                                        contentDescription = "Delete note",
                                        modifier = Modifier.size(iconSize),
                                    )
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.error,
                                    trailingIconColor = MaterialTheme.colorScheme.error,
                                ),
                                contentPadding = PaddingValues(horizontal = horizontalPadding)
                            )
                        }
                    }
                })
        },
        modifier = modifier.clickable(
            onClick = { focusManager.clearFocus() },
            indication = null,
            interactionSource = remember { MutableInteractionSource() }),
    )
    { innerPadding ->
        if (isFirstLoading) LoadingScreen(modifier = Modifier.padding(innerPadding))
        else {
            Column(
                modifier = Modifier
                    .padding(
                        start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                        end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                    )
                    .let { if (commentInputFocused) it.imePadding() else it }
                    .fillMaxSize()
            ) {
                when(modalBottomType) {
                    NoteModalBottomType.COVER -> {
                        CoverPickerModal(
                            currentColor = noteCoverColor,
                            onSave = {
                                coroutineScope.launch {
                                    val newCoverColor = it?.toArgb()

                                    val isSuccess = boardViewModel.updateNoteCover(
                                        boardId = boardId,
                                        noteListId = noteListId,
                                        docId = noteId,
                                        color = newCoverColor
                                    )
                                    if (!isSuccess) {
                                        snackbarHost.showSnackbar(
                                            message = "Change cover failed",
                                            withDismissAction = true,
                                        )
                                    }
                                    modalBottomType = null
                                }
                            },
                            onDismissRequest = { modalBottomType = null },
                        )
                    }

                    NoteModalBottomType.DESCRIPTION -> {
                        DescriptionEditModal(
                            currentDescription = note?.description,
                            onSave = {
                                coroutineScope.launch {
                                    val isSuccess = boardViewModel.updateNoteDescription(
                                        boardId = boardId,
                                        noteListId = noteListId,
                                        docId = noteId, description = it
                                    )
                                    if (!isSuccess) snackbarHost.showSnackbar(
                                        message = "Update note description failed",
                                        withDismissAction = true,
                                    )
                                    modalBottomType = null
                                }
                            }, onDismissRequest = { modalBottomType = null })
                    }

                    NoteModalBottomType.START_DATE -> {
                        DateTimePickerModal(
                            title = "Start Date",
                            currentDate = note?.startDate,
                            onSave = {
                                coroutineScope.launch {
                                    val isSuccess = boardViewModel.updateNoteStartDate(
                                        boardId = boardId,
                                        noteListId = noteListId,
                                        docId = noteId, dateTime = it
                                    )

                                    if (!isSuccess) snackbarHost.showSnackbar(
                                        message = "Update note start date failed",
                                        withDismissAction = true,
                                    )
                                    modalBottomType = null
                                }
                            },
                            onDismissRequest = { modalBottomType = null },
                            modifier = Modifier,
                        )
                    }

                    NoteModalBottomType.END_DATE -> {
                        DateTimePickerModal(
                            title = "Due Date",
                            currentDate = note?.endDate,
                            onSave = {
                                coroutineScope.launch {
                                    val isSuccess = boardViewModel.updateNoteEndDate(
                                        boardId = boardId,
                                        noteListId = noteListId,
                                        docId = noteId, dateTime = it
                                    )

                                    if (!isSuccess) snackbarHost.showSnackbar(
                                        message = "Update note end date failed",
                                        withDismissAction = true,
                                    )
                                    modalBottomType = null
                                }
                            },
                            onDismissRequest = { modalBottomType = null },
                            modifier = Modifier,
                        )
                    }

                    null -> null
                }
                LazyColumn(
                    state = lazyListState, modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val spacerWidth = 12.dp
                    val fontFamily = Roboto
                    val smallLabelTextStyle = TextStyle(
                        fontSize = 15.sp, lineHeight = 16.sp, letterSpacing = 0.sp,
                        fontFamily = fontFamily, fontWeight = FontWeight.Normal,
                    )
                    val mediumLabelTextStyle = TextStyle(
                        fontSize = 15.sp, lineHeight = 18.sp, letterSpacing = 0.sp,
                        fontFamily = fontFamily, fontWeight = FontWeight.Normal,
                    )
                    val verticalPadding = 20.dp
                    val horizontalPadding = 16.dp
                    val leadingIconSize = with(density) { 16.sp.toDp() + 2.dp }

                    item(key = "SpacerCover") {
                        if (noteCoverColor != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(innerPadding.calculateTopPadding())
                                    .background(noteCoverColor)
                            )
                        } else Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))
                    }
                    item(key = "Cover") {
                        Box(
                            contentAlignment = Alignment.BottomStart,
                            modifier = Modifier
                                .fillMaxWidth()
                                .let {
                                    val temp = it.height(innerPadding.calculateTopPadding())
                                    if (noteCoverColor == null) return@let temp
                                    return@let temp.background(noteCoverColor)
                                }
                                .padding(horizontal = horizontalPadding / 2, vertical = 8.dp)) {
                            val shape = RoundedCornerShape(15f)
                            Row(
                                modifier = Modifier
                                    .clickable(onClick = {
                                        modalBottomType = NoteModalBottomType.COVER
                                    })
                                    .clip(shape)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        shape = shape
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        shape = shape,
                                    )
                                    .padding(vertical = 8.dp, horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.fill_cover),
                                    contentDescription = "Cover",
                                    modifier = Modifier.size(leadingIconSize),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Cover",
                                    fontSize = 16.sp, lineHeight = 16.sp, letterSpacing = 0.sp,
                                    fontFamily = fontFamily, fontWeight = FontWeight.Normal,
                                )
                            }
                        }
                    }
                    if (note?.archived == true) {
                        item(key = "ArchiveStatus") {
                            val shape = RoundedCornerShape(15f)
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .clip(shape)
                                    .padding(horizontal = horizontalPadding)
                                    .background(
                                        color = MaterialTheme.colorScheme.error, shape = shape
                                    )
                                    .padding(vertical = 6.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.outline_archive),
                                    contentDescription = "Cover",
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Archived",
                                    fontSize = 12.sp, lineHeight = 12.sp, letterSpacing = 0.sp,
                                    fontFamily = fontFamily, fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onError,
                                )
                            }
                        }
                    }
                    item(key = "Name") {
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalPadding, vertical = 24.dp),
                        ) {
                            val fontSize = 22.sp
                            val iconSize = with(density) { fontSize.toDp() + 2.dp }
                            Icon(
                                painter = painterResource(
                                    if (note?.completed == true) R.drawable.fill_checkbox
                                    else R.drawable.outline_circle
                                ),
                                tint = if (note?.completed == true) MaterialTheme.colorScheme.success
                                else MaterialTheme.colorScheme.onSurface,
                                contentDescription = null,
                                modifier = Modifier.size(iconSize).clip(CircleShape)
                                    .clickable(onClick = {
                                        coroutineScope.launch {
                                            val isSuccess = boardViewModel.updateNoteComplete(
                                                boardId = boardId,
                                                noteListId = noteListId,
                                                docId = noteId,
                                                newState = note?.completed != true,
                                            )
                                            if (!isSuccess) {
                                                snackbarHost.showSnackbar(
                                                    message = "Mark note completed failed",
                                                    withDismissAction = true,
                                                )
                                            }
                                        }
                                    }).let {
                                        if (note?.completed == true) return@let it.background(
                                            MaterialTheme.colorScheme.onSurface
                                        )
                                        return@let it
                                    })
                            Spacer(modifier = Modifier.width(8.dp))
                            CustomTextField(
                                value = noteName,
                                onValueChange = { noteName = it },
                                placeholder = {
                                    Text(
                                        text = "Note Name",
                                        fontSize = fontSize,
                                        lineHeight = fontSize,
                                        fontFamily = fontFamily,
                                        letterSpacing = 0.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                                textStyle = TextStyle(
                                    fontSize = fontSize,
                                    lineHeight = fontSize,
                                    fontFamily = fontFamily,
                                    letterSpacing = 0.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                ),
                                containerColor = Color.Transparent,
                                modifier = Modifier
                                    .onFocusChanged { state ->
                                        val noteNameInitial = note?.name ?: ""
                                        if (!state.isFocused && noteName != noteNameInitial) {
                                            coroutineScope.launch {
                                                if (noteName.isBlank()) noteName = noteNameInitial
                                                else {
                                                    val isSuccess = boardViewModel.updateNoteName(
                                                        boardId = boardId,
                                                        noteListId = noteListId,
                                                        docId = noteId,
                                                        name = noteName,
                                                    )
                                                    if (!isSuccess) {
                                                        noteName = noteNameInitial
                                                        snackbarHost.showSnackbar(
                                                            message = "Update note name failed. Try again",
                                                            withDismissAction = true,
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                            )
                        }
                    }
                    item(key = "Description") {
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier
                                .clickable(onClick = {
                                    modalBottomType = NoteModalBottomType.DESCRIPTION
                                })
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(
                                    horizontal = horizontalPadding, vertical = verticalPadding
                                ),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_description),
                                contentDescription = "Note description",
                                modifier = Modifier.size(leadingIconSize),
                            )
                            Spacer(modifier = Modifier.width(spacerWidth))
                            Text(
                                text = if (note?.description.isNullOrEmpty()) "Add description"
                                else note?.description ?: "",
                                style = mediumLabelTextStyle,
                                color = if (note?.description.isNullOrEmpty()) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        HorizontalDivider()
                    }
                    item(key = "DateTime") {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Row(
                                modifier = Modifier
                                    .clickable(onClick = {
                                        modalBottomType = NoteModalBottomType.START_DATE
                                    })
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = horizontalPadding, vertical = verticalPadding
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.outline_clock),
                                    contentDescription = "Start date",
                                    modifier = Modifier.size(leadingIconSize),
                                )
                                Spacer(modifier = Modifier.width(spacerWidth))
                                Text(
                                    text = if (note?.startDate == null) "Start date"
                                    else DateFormater.format(
                                        timestamp = note!!.startDate!!,
                                        formatString = "'Starts' dd MMMM, yyyy 'at' HH:mm"
                                    ),
                                    style = mediumLabelTextStyle,
                                    color = if (note?.startDate == null) MaterialTheme.colorScheme.onSurfaceVariant
                                    else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(start = leadingIconSize + horizontalPadding + spacerWidth)
                            )
                            Row(
                                modifier = Modifier
                                    .clickable(onClick = {
                                        modalBottomType = NoteModalBottomType.END_DATE
                                    })
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = horizontalPadding, vertical = verticalPadding
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    painter = painterResource(
                                        if (note?.completed == true) R.drawable.outline_checkmark
                                        else R.drawable.outline_square
                                    ),
                                    contentDescription = "End date",
                                    tint = if (note?.endDate == null) Color.Transparent
                                    else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(leadingIconSize),
                                )
                                Spacer(modifier = Modifier.width(spacerWidth))
                                Text(
                                    text = if (note?.endDate == null) "End date"
                                    else DateFormater.format(
                                        timestamp = note!!.endDate!!,
                                        formatString = "'Due' dd MMMM, yyyy 'at' HH:mm"
                                    ),
                                    style = mediumLabelTextStyle,
                                    color = if (note?.endDate == null) MaterialTheme.colorScheme.onSurfaceVariant
                                    else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                    item(key = "CheckLists") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalPadding),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = verticalPadding)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.outline_checkmark),
                                    contentDescription = null,
                                    modifier = Modifier.size(leadingIconSize),
                                )
                                Spacer(modifier = Modifier.width(spacerWidth))
                                Text(text = "Checklists", style = smallLabelTextStyle)
                            }
                            Spacer(modifier = Modifier.width(spacerWidth))
                            IconButton(
                                onClick = {
                                    val newChecklist = Checklist()
                                    val isSuccess = boardViewModel.addNewChecklist(
                                        boardId = boardId,
                                        noteListId = noteListId,
                                        noteId
                                    )
                                    if (!isSuccess) {
                                        coroutineScope.launch {
                                            snackbarHost.showSnackbar(
                                                message = "Add new checklist failed",
                                                withDismissAction = true,
                                            )
                                        }
                                    }
                                },
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "Add new check list",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }
                    }
                    itemsIndexed(
                        items = checklists,
                        // 3. Use the docId as a unique key for stable item identification.
                        key = { _, item -> item.docId ?: UUID.randomUUID() }
                    ) { _, item ->
                        ChecklistItemBoard(
                            checklistBoard = item,
                            // 5. Pass a lambda for updating the name.
                            onChangeChecklistName = { newName ->
                                boardViewModel.updateChecklistName(
                                    boardId = boardId,
                                    noteListId = noteListId,
                                    noteId = noteId,
                                    checklistId = item.docId,
                                    newName = newName
                                )
                            },
                            // 6. Pass a lambda for deleting the item.
                            onDeleteChecklist = {
                                boardViewModel.removeChecklist(
                                    boardId = boardId,
                                    noteListId = noteListId,
                                    noteId = noteId,
                                    checklistId = item.docId
                                )
                            }
                        )
                    }
                    item(key = "CommentLabel") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = horizontalPadding, vertical = verticalPadding
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_comment),
                                contentDescription = "Attachments",
                                modifier = Modifier.size(leadingIconSize),
                            )
                            Spacer(modifier = Modifier.width(spacerWidth))
                            Text(text = "Comments", style = smallLabelTextStyle)
                        }
                    }
                    if (commentList.isEmpty()) item(key = "EmptyComment") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(vertical = 24.dp, horizontal = 32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "There are no comments on this note",
                                style = mediumLabelTextStyle,
                                textAlign = TextAlign.Center,
                            )
                        }
                        HorizontalDivider()
                    }
                    else itemsIndexed(items = commentList) { index, comment ->
                        CommentItem(comment = comment)
                    }
                    item(key = "BottomSpacer") { Spacer(modifier = Modifier.height(160.dp)) }
                }
                CommentInputSection(
                    commentText = commentText,
                    onCommentTextChange = { commentText = it },
                    onPostComment = { },
                    modifier = Modifier
                        .onFocusChanged { commentInputFocused = it.isFocused }
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .let {
                            if (commentInputFocused) return@let it
                            return@let it.padding(bottom = innerPadding.calculateBottomPadding())
                        }
                )
            }
        }
    }
}*/

package com.apcs.worknestapp.ui.screens.note_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.note.NoteViewModel
import com.apcs.worknestapp.domain.logic.DateFormater
import com.apcs.worknestapp.ui.components.LoadingScreen
import com.apcs.worknestapp.ui.components.inputfield.CustomTextField
import com.apcs.worknestapp.ui.components.notedetail.Attachment
import com.apcs.worknestapp.ui.components.notedetail.AttachmentOption
import com.apcs.worknestapp.ui.components.notedetail.AttachmentOptionsDropdownMenu
import com.apcs.worknestapp.ui.components.notedetail.Comment
import com.apcs.worknestapp.ui.components.notedetail.CommentInputSection
import com.apcs.worknestapp.ui.components.notedetail.CommentItem
import com.apcs.worknestapp.ui.components.notedetail.CoverPickerModal
import com.apcs.worknestapp.ui.components.notedetail.DateTimePickerModal
import com.apcs.worknestapp.ui.components.notedetail.DescriptionEditModal
import com.apcs.worknestapp.ui.components.notedetail.WorklistItem
import com.apcs.worknestapp.ui.components.notedetail.WorklistItemUI
import com.apcs.worknestapp.ui.components.topbar.CustomTopBar
import com.apcs.worknestapp.ui.theme.Roboto
import com.apcs.worknestapp.ui.theme.success
import com.apcs.worknestapp.utils.ColorUtils
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.util.UUID

enum class NoteModalBottomType {
    COVER,
    DESCRIPTION,
    START_DATE,
    END_DATE,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: String,
    snackbarHost: SnackbarHostState,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    noteViewModel: NoteViewModel = hiltViewModel(),
) {
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    var isFirstLoading by remember { mutableStateOf(true) }
    var modalBottomType by remember { mutableStateOf<NoteModalBottomType?>(null) }

    //NoteState
    var noteName by remember { mutableStateOf("") }
    var noteCover by remember { mutableStateOf<Int?>(null) }
    val noteCoverColor = if (noteCover != null) ColorUtils.safeParse(noteCover!!) else null
    var noteCompleted by remember { mutableStateOf(false) }
    var noteArchived by remember { mutableStateOf(false) }
    var noteDescription by remember { mutableStateOf("") }
    var noteStartDate by remember { mutableStateOf<Timestamp?>(null) }
    var noteEndDate by remember { mutableStateOf<Timestamp?>(null) }

    var workList by remember { mutableStateOf(emptyList<WorklistItem>()) }
    var history by remember { mutableStateOf(emptyList<String>()) }

    var currentBoard by remember { mutableStateOf<String?>("inbox") }
    var quickMenu by remember { mutableStateOf(false) }

    var commentText by remember { mutableStateOf("") }
    var commentList by remember { mutableStateOf(emptyList<Comment>()) }

    var showAttachmentMenu by remember { mutableStateOf(false) }
    var attachmentsList by remember { mutableStateOf(emptyList<Attachment>()) }

    //LayoutState
    val lazyListState = rememberLazyListState()
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceColorOverlay = surfaceColor.copy(alpha = 0.05f)
    var topBarBackground by remember { mutableStateOf(surfaceColorOverlay) }

    LaunchedEffect(Unit) {
        isFirstLoading = true
        val note = noteViewModel.getNote(noteId)
        if (note == null) {
            noteViewModel.deleteNote(noteId)
            snackbarHost.showSnackbar(
                message = "Load note failed. Note not founded",
                withDismissAction = true,
            )
            navController.popBackStack()
        } else {
            noteName = note.name ?: ""
            noteCover = note.cover
            noteCover?.let { topBarBackground = surfaceColorOverlay }
            noteCompleted = note.completed ?: false
            noteArchived = note.archived ?: false
            noteDescription = note.description ?: ""
            noteStartDate = note.startDate
            noteEndDate = note.endDate
        }

        isFirstLoading = false
    }

    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.firstVisibleItemIndex }.collect {
            topBarBackground = if (it != 0) surfaceColor
            else surfaceColorOverlay
        }
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                field = "",
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
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            modifier = Modifier
                                .size(28.dp)
                                .rotate(-90f)
                        )
                    }
                }
            )
        },
        modifier = modifier.clickable(
            onClick = { focusManager.clearFocus() },
            indication = null, interactionSource = remember { MutableInteractionSource() }
        ),
    ) { innerPadding ->
        if (isFirstLoading) LoadingScreen(modifier = Modifier.padding(innerPadding))
        else {
            Box(
                modifier = Modifier
                    .padding(
                        start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                        end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                    )
                    .imePadding()
                    .fillMaxSize()
            ) {
                when(modalBottomType) {
                    NoteModalBottomType.COVER -> {
                        CoverPickerModal(
                            currentColor = noteCover?.let { ColorUtils.safeParse(it) },
                            onSave = {
                                coroutineScope.launch {
                                    val newCoverColor = it?.toArgb()

                                    val isSuccess = noteViewModel.updateNoteCover(
                                        docId = noteId,
                                        color = newCoverColor
                                    )
                                    if (isSuccess) noteCover = newCoverColor
                                    else {
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
                            currentDescription = noteDescription,
                            onSave = {
                                coroutineScope.launch {
                                    val isSuccess = noteViewModel.updateNoteDescription(
                                        docId = noteId,
                                        description = it
                                    )
                                    if (isSuccess) noteDescription = it
                                    else {
                                        snackbarHost.showSnackbar(
                                            message = "Update note description failed",
                                            withDismissAction = true,
                                        )
                                    }
                                    modalBottomType = null
                                }
                            },
                            onDismissRequest = { modalBottomType = null }
                        )
                    }

                    NoteModalBottomType.START_DATE -> {
                        DateTimePickerModal(
                            title = "Start Date",
                            currentDate = noteStartDate,
                            onSave = {
                                coroutineScope.launch {
                                    val isSuccess = noteViewModel.updateNoteStartDate(
                                        docId = noteId,
                                        dateTime = it
                                    )

                                    if (isSuccess) noteStartDate = it
                                    else {
                                        snackbarHost.showSnackbar(
                                            message = "Update note start date failed",
                                            withDismissAction = true,
                                        )
                                    }
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
                            currentDate = noteEndDate,
                            onSave = {
                                coroutineScope.launch {
                                    val isSuccess = noteViewModel.updateNoteEndDate(
                                        docId = noteId,
                                        dateTime = it
                                    )

                                    if (isSuccess) noteEndDate = it
                                    else {
                                        snackbarHost.showSnackbar(
                                            message = "Update note end date failed",
                                            withDismissAction = true,
                                        )
                                    }
                                    modalBottomType = null
                                }
                            },
                            onDismissRequest = { modalBottomType = null },
                            modifier = Modifier,
                        )
                    }

                    null -> null
                }
                CommentInputSection(
                    commentText = commentText,
                    onCommentTextChange = { commentText = it },
                    onPostComment = { },
                    modifier = Modifier
                        .align(alignment = Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(bottom = innerPadding.calculateBottomPadding())
                        .zIndex(10f)
                )
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .padding(bottom = innerPadding.calculateBottomPadding())
                        .imePadding()
                        .fillMaxSize()
                        .zIndex(1f),
                )
                {
                    val spacerWidth = 12.dp
                    val fontFamily = Roboto
                    val smallLabelTextStyle = TextStyle(
                        fontSize = 14.sp, lineHeight = 16.sp, letterSpacing = 0.sp,
                        fontFamily = fontFamily, fontWeight = FontWeight.Normal,
                    )
                    val mediumLabelTextStyle = TextStyle(
                        fontSize = 15.sp, lineHeight = 18.sp, letterSpacing = 0.sp,
                        fontFamily = fontFamily, fontWeight = FontWeight.Normal,
                    )
                    val verticalPadding = 20.dp
                    val horizontalPadding = 12.dp
                    val leadingIconSize = with(density) { 16.sp.toDp() + 2.dp }

                    item(key = "SpacerCover") {
                        if (noteCoverColor != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(innerPadding.calculateTopPadding() * 2)
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
                                .padding(horizontal = horizontalPadding, vertical = 8.dp)
                        ) {
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
                                    .padding(vertical = 8.dp, horizontal = 16.dp),
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
                    item(key = "Name") {
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalPadding, vertical = 24.dp),
                        ) {
                            val fontSize = 20.sp
                            Icon(
                                painter = painterResource(
                                    if (!noteCompleted) R.drawable.outline_circle
                                    else R.drawable.fill_checkbox
                                ),
                                tint = if (!noteCompleted) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.success,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(with(density) { fontSize.toDp() + 2.dp })
                                    .clip(CircleShape)
                                    .clickable(onClick = {
                                        coroutineScope.launch {
                                            noteCompleted = !noteCompleted
                                            val isSuccess = noteViewModel.updateNoteComplete(
                                                docId = noteId,
                                                newState = noteCompleted,
                                            )
                                            if (!isSuccess) {
                                                noteCompleted = !noteCompleted
                                                snackbarHost.showSnackbar(
                                                    message = "Mark note completed failed",
                                                    withDismissAction = true,
                                                )
                                            }
                                        }
                                    })
                                    .let {
                                        if (noteCompleted) return@let it.background(MaterialTheme.colorScheme.onSurface)
                                        return@let it
                                    }
                            )
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
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium,
                                    )
                                },
                                textStyle = TextStyle(
                                    fontSize = fontSize,
                                    lineHeight = fontSize,
                                    fontFamily = fontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground
                                ),
                                containerColor = Color.Transparent,
                                modifier = Modifier
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
                                    horizontal = horizontalPadding,
                                    vertical = verticalPadding
                                ),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_description),
                                contentDescription = "Note description",
                                modifier = Modifier.size(leadingIconSize),
                            )
                            Spacer(modifier = Modifier.width(spacerWidth))
                            Text(
                                text = noteDescription.ifEmpty { "Add a description" },
                                style = mediumLabelTextStyle,
                                color = if (noteDescription.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
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
                                    text = if (noteStartDate == null) "Start date"
                                    else DateFormater.format(
                                        timestamp = noteStartDate!!,
                                        formatString = "'Starts' dd MMMM, yyyy 'at' HH:mm"
                                    ),
                                    style = mediumLabelTextStyle,
                                    color = if (noteStartDate == null) MaterialTheme.colorScheme.onSurfaceVariant
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
                                        if (noteCompleted) R.drawable.outline_checkmark
                                        else R.drawable.outline_square
                                    ),
                                    contentDescription = "End date",
                                    tint = if (noteEndDate == null) Color.Transparent
                                    else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(leadingIconSize),
                                )
                                Spacer(modifier = Modifier.width(spacerWidth))
                                Text(
                                    text = if (noteEndDate == null) "End date"
                                    else DateFormater.format(
                                        timestamp = noteEndDate!!,
                                        formatString = "'Due' dd MMMM, yyyy 'at' HH:mm"
                                    ),
                                    style = mediumLabelTextStyle,
                                    color = if (noteEndDate == null) MaterialTheme.colorScheme.onSurfaceVariant
                                    else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                )
                            }
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
                                    contentDescription = "Attachments",
                                    modifier = Modifier.size(leadingIconSize),
                                )
                                Spacer(modifier = Modifier.width(spacerWidth))
                                Text(text = "Checklists", style = smallLabelTextStyle)
                            }
                            Spacer(modifier = Modifier.width(spacerWidth))
                            IconButton(
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                onClick = {
                                    val newWorklistItem =
                                        WorklistItem(name = "New List", tasks = emptyList())
                                    workList = workList + newWorklistItem
                                },
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "Add new task list",
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                        }
                    }
                    itemsIndexed(items = workList, key = { _, item -> item.id }) { idx, item ->
                        WorklistItemUI(
                            worklistItem = item,
                            onWorklistItemChange = { updatedItem ->
                                val newList = workList.toMutableList()
                                newList[idx] = updatedItem
                                workList = newList
                            },
                            onDeleteWorklistItem = {
                                workList = workList.filterNot { it.id == item.id }
                            }
                        )
                        if (idx + 1 < workList.size) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = 4.dp,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                    item(key = "Attachments") {
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
                                    .padding(vertical = verticalPadding),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.fill_attachment),
                                    contentDescription = "Attachments",
                                    modifier = Modifier.size(leadingIconSize),
                                )
                                Spacer(modifier = Modifier.width(spacerWidth))
                                Text(text = "Attachments", style = smallLabelTextStyle)
                            }
                            Box { // Box to anchor the DropdownMenu
                                IconButton(
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    onClick = { showAttachmentMenu = true }
                                ) {
                                    Icon(
                                        Icons.Filled.Add,
                                        contentDescription = "Add new task list",
                                        modifier = Modifier.size(28.dp),
                                    )
                                }
                                // This is the Submenu
                                AttachmentOptionsDropdownMenu(
                                    expanded = showAttachmentMenu,
                                    onDismissRequest = { showAttachmentMenu = false },
                                    onOptionSelected = { selectedOption ->
                                        showAttachmentMenu = false
                                        // --- Placeholder  --- /////////////////////////////////////////////////
                                        when(selectedOption) {
                                            AttachmentOption.UPLOAD_FILE -> {
                                                println("Option Selected: Upload File")
                                                // TODO: Placeholder Attach File
                                                attachmentsList =
                                                    (attachmentsList + Attachment(
                                                        UUID.randomUUID().toString(),
                                                        "Placeholder File",
                                                        "File"
                                                    ))
                                            }

                                            AttachmentOption.ADD_LINK -> {
                                                println("Option Selected: Add Link")
                                                attachmentsList =
                                                    (attachmentsList + Attachment(
                                                        UUID.randomUUID().toString(),
                                                        "Placeholder Link",
                                                        "Link"
                                                    ))
                                            }

                                            AttachmentOption.ADD_IMAGE_FROM_GALLERY -> {
                                                println("Option Selected: Add Image from Gallery")
                                                attachmentsList = attachmentsList + Attachment(
                                                    UUID.randomUUID().toString(),
                                                    "Placeholder Gallery Image",
                                                    "Image"
                                                )
                                            }

                                            AttachmentOption.TAKE_PHOTO -> {
                                                println("Option Selected: Take Photo")
                                                attachmentsList = attachmentsList + Attachment(
                                                    UUID.randomUUID().toString(),
                                                    "Placeholder Camera Photo",
                                                    "Image"
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        /*                        if (attachmentsList.isNotEmpty()) {
                                                    attachmentsList.forEach { attachment ->
                                                        //TODO: Add AttachmentItemRow
                                                        HorizontalDivider()
                                                    }
                                                }*/
                    }
                    item(key = "CommentLabel") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = horizontalPadding,
                                    vertical = verticalPadding
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
                    }
                    else itemsIndexed(items = commentList) { index, comment ->
                        CommentItem(comment = comment)
                    }
                    item(key = "BottomSpacer") { Spacer(modifier = Modifier.height(240.dp)) }
                }
            }
        }
    }
}

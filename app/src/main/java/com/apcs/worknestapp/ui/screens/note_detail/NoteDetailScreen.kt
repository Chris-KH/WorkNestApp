package com.apcs.worknestapp.ui.screens.note_detail

import android.icu.util.Calendar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.apcs.worknestapp.LocalAuthViewModel

import com.apcs.worknestapp.ui.components.bottombar.MainBottomBar
import com.apcs.worknestapp.ui.components.notedetail.Attachment
import com.apcs.worknestapp.ui.components.notedetail.AttachmentOption
import com.apcs.worknestapp.ui.components.notedetail.AttachmentOptionsDropdownMenu
import com.apcs.worknestapp.ui.components.notedetail.Comment
import com.apcs.worknestapp.ui.components.notedetail.CommentInputSection
import com.apcs.worknestapp.ui.components.notedetail.CommentItem
import com.apcs.worknestapp.ui.components.notedetail.TimePickerDialog
import com.apcs.worknestapp.ui.components.notedetail.User
import com.apcs.worknestapp.ui.components.notedetail.WorklistItem
import com.apcs.worknestapp.ui.components.notedetail.WorklistItemUI
import com.apcs.worknestapp.ui.components.topbar.MainTopBar
import com.apcs.worknestapp.ui.screens.Screen

import java.util.Date
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
    noteId: String,
) {
    val authViewModel = LocalAuthViewModel.current
    val userProfile by authViewModel.profile.collectAsState()
    val focusManager = LocalFocusManager.current

    //TODO:to be replaced with actual model:
    var noteName by remember { mutableStateOf("Placeholder") }
    var checked by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("Placeholder") }


    var startDate by remember { mutableStateOf<Long?>(null) }
    var startHour by remember { mutableStateOf<Int?>(null) }
    var startMinute by remember { mutableStateOf<Int?>(null) }

    var endDate by remember { mutableStateOf<Long?>(null) }
    var endHour by remember { mutableStateOf<Int?>(null) }
    var endMinute by remember { mutableStateOf<Int?>(null) }

    var workList by remember { mutableStateOf(emptyList<WorklistItem>()) }

    var history by remember { mutableStateOf(emptyList<String>()) }

    var currentBoard by remember { mutableStateOf<String?>("inbox") }
    var quickMenu by remember { mutableStateOf(false) }

    var commentText by remember { mutableStateOf("") }
    var commentList by remember { mutableStateOf(emptyList<Comment>()) }

    var showStartDatePickerDialog by remember { mutableStateOf(false) }
    var showStartTimePickerDialog by remember { mutableStateOf(false) }
    var showEndDatePickerDialog by remember { mutableStateOf(false) }
    var showEndTimePickerDialog by remember { mutableStateOf(false) }

    var showAttachmentMenu by remember { mutableStateOf(false) }
    var attachmentsList by remember { mutableStateOf(emptyList<Attachment>()) }
    /////////////////////////////////////////////////////////

    Scaffold(
        topBar = {
            MainTopBar(
                title = noteName,
                actions = {
                    NoteDetailScreenTopBarActions(
                        onEditClick = {
                            // TODO: Implement actual NoteDetail edit logic
                        },
                        onDeleteAllClick = {
                            // TODO: Implement actual delete
                        }
                    )
                }
            )
        },
        bottomBar = {
            MainBottomBar(
                currentScreen = Screen.Note,
                navController = navController,
            )
        },
        modifier = modifier,
    )
    { innerPadding ->

        val interactionSource = remember { MutableInteractionSource() }
        val isFocused by interactionSource.collectIsFocusedAsState()
        val imePadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
        LazyColumn(
            modifier = Modifier
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr)
                )
                .padding(
                    bottom = if (isFocused && imePadding > 0.dp) 0.dp
                    else innerPadding.calculateBottomPadding()
                )
                .imePadding()
                .fillMaxSize(),
        )
        {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = noteName,
                            onValueChange = { noteName = it },
                            label = { Text("Note Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { checked = !checked }
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { checked = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Done")
                        }
                    }
                }
            }
            item {
                NoteScheduleCard(
                    startDate = startDate,
                    startHour = startHour,
                    startMinute = startMinute,
                    onSelectStartDateClick = { showStartDatePickerDialog = true },
                    onSelectStartTimeClick = {
                        if (startDate != null) showStartTimePickerDialog = true
                    },
                    endDate = endDate,
                    endHour = endHour,
                    endMinute = endMinute,
                    onSelectEndDateClick = { showEndDatePickerDialog = true },
                    onSelectEndTimeClick = { if (endDate != null) showEndTimePickerDialog = true }
                )
            }
            item {
                if (showStartDatePickerDialog) {
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = startDate ?: System.currentTimeMillis(),
                        initialDisplayMode = DisplayMode.Picker // Or DisplayMode.Input
                    )
                    DatePickerDialog(
                        onDismissRequest = { showStartDatePickerDialog = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    startDate = datePickerState.selectedDateMillis
                                    showStartDatePickerDialog = false
                                }
                            ) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showStartDatePickerDialog = false
                            }) { Text("Cancel") }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }
                if (showStartTimePickerDialog) {
                    TimePickerDialog(
                        onDismissRequest = { showStartTimePickerDialog = false },
                        onConfirm = { hour, minute ->
                            startHour = hour
                            startMinute = minute
                            showStartTimePickerDialog = false
                        },
                        initialHour = startHour ?: Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                        initialMinute = startMinute ?: Calendar.getInstance().get(Calendar.MINUTE)
                    )
                }
                if (showEndDatePickerDialog) {
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = endDate ?: startDate
                        ?: System.currentTimeMillis(),
                        initialDisplayMode = DisplayMode.Picker
                    )
                    DatePickerDialog(
                        onDismissRequest = { showEndDatePickerDialog = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    endDate = datePickerState.selectedDateMillis
                                    showEndDatePickerDialog = false
                                }
                            ) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showEndDatePickerDialog = false
                            }) { Text("Cancel") }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }
                if (showEndTimePickerDialog) {
                    TimePickerDialog(
                        onDismissRequest = { showEndTimePickerDialog = false },
                        onConfirm = { hour, minute ->
                            endHour = hour
                            endMinute = minute
                            showEndTimePickerDialog = false
                        },
                        initialHour = endHour ?: Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                        initialMinute = endMinute ?: Calendar.getInstance().get(Calendar.MINUTE)
                    )
                }

            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Attachments", style = MaterialTheme.typography.titleMedium)
                            Box { // Box to anchor the DropdownMenu
                                IconButton(onClick = { showAttachmentMenu = true }) {
                                    Icon(Icons.Filled.Add, contentDescription = "Add Attachment")
                                }
                                // This is the Submenu
                                AttachmentOptionsDropdownMenu(
                                    expanded = showAttachmentMenu,
                                    onDismissRequest = { showAttachmentMenu = false },
                                    onOptionSelected = { selectedOption ->
                                        showAttachmentMenu = false
                                        // --- Placeholder  --- /////////////////////////////////////////////////
                                        when (selectedOption) {
                                            AttachmentOption.UPLOAD_FILE -> {
                                                println("Option Selected: Upload File")
                                                // TODO: Placeholder Attach File
                                                attachmentsList =
                                                    (attachmentsList + Attachment(
                                                        UUID.randomUUID().toString(),
                                                        "Placeholder File",
                                                        "File"
                                                    )) as List<Attachment>
                                            }

                                            AttachmentOption.ADD_LINK -> {
                                                println("Option Selected: Add Link")
                                                attachmentsList =
                                                    (attachmentsList + Attachment(
                                                        UUID.randomUUID().toString(),
                                                        "Placeholder Link",
                                                        "Link"
                                                    )) as List<Attachment>
                                            }

                                            AttachmentOption.ADD_IMAGE_FROM_GALLERY -> {
                                                println("Option Selected: Add Image from Gallery")
                                                attachmentsList = attachmentsList + Attachment(
                                                    UUID.randomUUID().toString(),
                                                    "Placeholder Gallery Image",
                                                    "Image"
                                                ) as List<Attachment>
                                            }

                                            AttachmentOption.TAKE_PHOTO -> {
                                                println("Option Selected: Take Photo")
                                                attachmentsList = attachmentsList + Attachment(
                                                    UUID.randomUUID().toString(),
                                                    "Placeholder Camera Photo",
                                                    "Image"
                                                ) as List<Attachment>
                                            }
                                        }
                                        // --- End Placeholder ---////////////////////////////////////////////////////
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (attachmentsList.isEmpty()) {
                            Text("No attachments yet. Click '+' to add.")
                        } else {
                            attachmentsList.forEach { attachment ->
                                //TODO: Add AttachmentItemRow
                                HorizontalDivider()
                            }
                        }


                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Worklist", style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = {
                                val newWorklistItem =
                                    WorklistItem(name = "New List", tasks = emptyList())
                                workList = workList + newWorklistItem
                            }) {
                                Icon(Icons.Filled.Add, contentDescription = "Add new task list")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        if (workList.isEmpty()) {
                            Text("No task lists yet. Add one!")
                        } else {
                            workList.forEachIndexed { listIndex, worklistItem ->
                                WorklistItemUI(
                                    worklistItem = worklistItem,
                                    onWorklistItemChange = { updatedItem ->
                                        val newList = workList.toMutableList()
                                        newList[listIndex] = updatedItem
                                        workList = newList
                                    },
                                    onDeleteWorklistItem = {
                                        workList = workList.filterNot { it.id == worklistItem.id }
                                    }
                                )
                                if (listIndex < workList.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        thickness = 4.dp,
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Comments",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = 8.dp
                    )
                )
            }

            if (commentList.isEmpty()) {
                item {
                    Text(
                        text = "No comments yet. Be the first to comment!",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(commentList.size) { index ->
                    val comment = commentList[index]
                    CommentItem(comment = comment)
                }
            }

            item {
                CommentInputSection(
                    commentText = commentText,
                    onCommentTextChange = { commentText = it },
                    onPostComment = {
                        userProfile?.let { profile ->
                            if (commentText.isNotBlank()) {
                                val newComment = Comment(
                                    id = UUID.randomUUID().toString(),
                                    text = commentText,
                                    author = User(
                                        name = profile.name ?: "Anonymous",
                                        avatarUrl = profile.avatar ?: ""
                                    ),
                                    timestamp = Date()
                                )
                                commentList = commentList + newComment
                                commentText = ""
                                focusManager.clearFocus()
                            }
                        } ?: run {
                        }
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}
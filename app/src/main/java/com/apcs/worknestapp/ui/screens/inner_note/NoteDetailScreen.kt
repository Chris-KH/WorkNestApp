package com.apcs.worknestapp.ui.screens.inner_note

import android.icu.util.Calendar
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import coil3.compose.rememberAsyncImagePainter
import com.apcs.worknestapp.LocalAuthViewModel
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.components.bottombar.MainBottomBar
import com.apcs.worknestapp.ui.components.topbar.MainTopBar
import com.apcs.worknestapp.ui.screens.Screen
import com.google.android.gms.fido.fido2.api.common.Attachment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


//data class User(
//    val id: String,
//    val userName: String,
//    val profilePicUrl: String
//)
data class Task(
    val id: String = UUID.randomUUID().toString(),
    var text: String,
    var isCompleted: Boolean
)

data class WorklistItem(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var tasks: List<Task>,
    var isCollapsed: Boolean = false // For collapse/expand functionality
)
data class Attachment(val id: String, val name: String, val type: String)
enum class AttachmentOption {
    UPLOAD_FILE,
    ADD_LINK,
    ADD_IMAGE_FROM_GALLERY,
    TAKE_PHOTO
    // Add other options as needed
}



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
    //to be replaced with actual model:
    var noteName by remember { mutableStateOf("Placeholder") }
    var noteText by remember { mutableStateOf("") }
    var showSubMenu by remember { mutableStateOf(false) }
    var checked by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("Placeholder") }


    var startDate by remember { mutableStateOf<Long?>(null) }
    var startHour by remember { mutableStateOf<Int?>(null) }
    var startMinute by remember { mutableStateOf<Int?>(null) }

    var endDate by remember { mutableStateOf<Long?>(null) }
    var endHour by remember { mutableStateOf<Int?>(null) }
    var endMinute by remember { mutableStateOf<Int?>(null) }

    var taskList by remember { mutableStateOf(emptyList<Task>()) }
    var workList by remember { mutableStateOf(emptyList<WorklistItem>()) }
    var history by remember { mutableStateOf(emptyList<String>()) }

    var currentBoard by remember { mutableStateOf<String?>("inbox") }
    var quickMenu by remember { mutableStateOf(false) }

    var commentText by remember { mutableStateOf("") }
    var commentList by remember { mutableStateOf(emptyList<Comment>()) }
    //comment
    // var attachments
    //var commentText by remember { mutableStateOf("") }
    //var commentList by remember { mutableStateOf(emptyList<Comment>()) }
    // background ????
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
                currentScreen = Screen.Note,
                actions = {
                    IconButton(onClick = { showSubMenu = true }) {
                        Icon(
                            painter = painterResource(R.drawable.symbol_three_dot),
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(-90f)
                        )
                    }
                    DropdownMenu(
                        expanded = showSubMenu,
                        onDismissRequest = { showSubMenu = false },
                        containerColor = MaterialTheme.colorScheme.surface,
                        shadowElevation = 32.dp,
                        shape = RoundedCornerShape(25f),
                        modifier = Modifier.widthIn(min = 160.dp),
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Edit",
                                    fontSize = 14.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            },
                            onClick = {}, //  onEditClick() },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Delete all",
                                    fontSize = 14.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            },
                            onClick = {},// onDeleteAllClick,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete All",
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        )
                    }
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
                            Text("Mark as Done")
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
                        Text("Schedule", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(12.dp))

                        // --- Start Date & Time ---
                        Text("Start", style = MaterialTheme.typography.titleSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(onClick = { showStartDatePickerDialog = true }) {
                                Text(
                                    startDate?.let {
                                        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(
                                            it
                                        )
                                    } ?: "Select Start Date"
                                )
                            }
                            Button(
                                onClick = {
                                    if (startDate != null) showStartTimePickerDialog = true
                                },
                                enabled = startDate != null
                            ) {
                                Text(
                                    if (startHour != null && startMinute != null) {
                                        String.format(
                                            Locale.getDefault(),
                                            "%02d:%02d",
                                            startHour,
                                            startMinute
                                        )
                                    } else {
                                        "Select Time"
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // --- End Date & Time ---
                        Text("End", style = MaterialTheme.typography.titleSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(onClick = { showEndDatePickerDialog = true }) {
                                Text(
                                    endDate?.let {
                                        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(
                                            it
                                        )
                                    } ?: "Select End Date"
                                )
                            }
                            Button(
                                onClick = { if (endDate != null) showEndTimePickerDialog = true },
                                enabled = endDate != null
                            ) {
                                Text(
                                    if (endHour != null && endMinute != null) {
                                        String.format(
                                            Locale.getDefault(),
                                            "%02d:%02d",
                                            endHour,
                                            endMinute
                                        )
                                    } else {
                                        "Select Time"
                                    }
                                )
                            }
                        }
                    }
                }

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
                                    // Optionally open time picker right after
                                    // if (startDate != null) showStartTimePickerDialog = true
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
                    TimePickerDialog( // Calling YOUR Composable
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
                                                // Later: filePickerLauncher.launch("*/*")
                                                // For now: Add a dummy item
                                                attachmentsList =
                                                    (attachmentsList + Attachment(UUID.randomUUID().toString(), "Placeholder File", "File")) as List<Attachment>
                                            }
                                            AttachmentOption.ADD_LINK -> {
                                                println("Option Selected: Add Link")
                                                attachmentsList =
                                                    (attachmentsList + Attachment(UUID.randomUUID().toString(), "Placeholder Link", "Link")) as List<Attachment>
                                            }
                                            AttachmentOption.ADD_IMAGE_FROM_GALLERY -> {
                                                println("Option Selected: Add Image from Gallery")
                                                attachmentsList = attachmentsList + Attachment(UUID.randomUUID().toString(), "Placeholder Gallery Image", "Image") as List<Attachment>
                                            }
                                            AttachmentOption.TAKE_PHOTO -> {
                                                println("Option Selected: Take Photo")
                                                attachmentsList = attachmentsList + Attachment(UUID.randomUUID().toString(), "Placeholder Camera Photo", "Image") as List<Attachment>
                                            }
                                        }
                                        // --- End Placeholder ---////////////////////////////////////////////////////
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Display area for attachments (very basic for now)
                        if (attachmentsList.isEmpty()) {
                            Text("No attachments yet. Click '+' to add.")
                        } else {
                            attachmentsList.forEach { attachment ->
                                // Replace with your AttachmentItemRow later
//                                ListItem(
//                                    headlineContent = { Text(attachment.name) },
//                                    supportingContent = { Text("Type: ${attachment.t}") }
//                                )
                                HorizontalDivider()
                            }
                        }

                        // The Text(text = noteName) and Text(text = noteText) from your original comment
                        // would go here IF they are specific to this "Attachments" card's context.
                        // Otherwise, they belong in a general note content card.
                        // For example:
                        // Spacer(modifier = Modifier.height(16.dp))
                        // Text("Contextual Note Name for Attachments: ${noteName}", style = MaterialTheme.typography.bodyLarge)
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
                                // Add a new empty WorklistItem
                                val newWorklistItem = WorklistItem(name = "New List", tasks = emptyList())
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
                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
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
                            CommentItem(comment = comment) // Assuming CommentItem is defined as before
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
                                    // Handle case where userProfile is null (e.g., show a message or disable commenting)
                                    // You might want to show a snackbar:
                                    // coroutineScope.launch { snackbarHost.showSnackbar("You need to be logged in to comment.") }
                                }
                            },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
        }
    }
}


data class User(
    //val id: String,
    val name: String?, // Was userName, changed to name for consistency with ProfileHeader
    val avatarUrl: String?, // Was profilePicUrl, changed for consistency
    //val email: String?,     // Add if you want to store/display email per comment user
    //val pronouns: String?  // Add if you want to store/display pronouns per comment user
    // Add any other fields you need for a comment's author
)

data class Comment(
    val id: String,
    val text: String,
    val author: User, // Changed from 'user' to 'author' for clarity
    val timestamp: Date,
    val replies: List<Comment> = emptyList()
)
@Composable
fun CommentAuthorHeader(
    author: User,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = author.avatarUrl,
                placeholder = painterResource(R.drawable.fade_avatar_fallback),
                error = painterResource(R.drawable.fade_avatar_fallback)
            ),
            contentDescription = "${author.name ?: "User"}'s profile picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = author.name ?: "Anonymous",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
@Composable
fun CommentItem(comment: Comment, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Use the new CommentAuthorHeader
            CommentAuthorHeader(author = comment.author)

            Spacer(modifier = Modifier.height(4.dp)) // Adjusted spacer
            Text(
                text = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(comment.timestamp),                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = comment.text, style = MaterialTheme.typography.bodyMedium)

            // Optional: Display replies
            if (comment.replies.isNotEmpty()) {
                // ... (reply logic remains the same) ...
            }
        }
    }
}
@Composable
fun CommentInputSection(
    commentText: String,
    onCommentTextChange: (String) -> Unit,
    onPostComment: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = commentText,
            onValueChange = onCommentTextChange,
            label = { Text("Add a comment...") },
            modifier = Modifier.weight(1f),
            maxLines = 5
        )
        IconButton(onClick = onPostComment, enabled = commentText.isNotBlank()) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Post comment",
                tint = if (commentText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.4f
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    initialHour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
    initialMinute: Int = Calendar.getInstance().get(Calendar.MINUTE),
    is24Hour: Boolean = true
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = is24Hour
    )

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                onConfirm(timePickerState.hour, timePickerState.minute)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        },
        title = { Text("Select Time") },
        text = {
            TimeInput(state = timePickerState)
        },
        properties = DialogProperties(dismissOnClickOutside = true)
    )
}

@Composable
fun AttachmentOptionsDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onOptionSelected: (AttachmentOption) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        DropdownMenuItem(
            text = { Text("Upload File") },
            onClick = { //onOptionSelected(AttachmentOption.UPLOAD_FILE)
                 },
            //leadingIcon = { Icon(Icons.Filled.AttachFile, contentDescription = "Upload File") }
        )
        DropdownMenuItem(
            text = { Text("Add Link") },
            onClick = { },
                //onOptionSelected(AttachmentOption.ADD_LINK)
            //leadingIcon = { Icon(Icons.Filled.Link, contentDescription = "Add Link") }
        )
        HorizontalDivider() // Optional: to group image options
        DropdownMenuItem(
            text = { Text("Image from Gallery") },
            onClick = { //onOptionSelected(AttachmentOption.ADD_IMAGE_FROM_GALLERY)
                      },
            //leadingIcon = { Icon(Icons.Filled.Image, contentDescription = "Image from Gallery") }
        )
        DropdownMenuItem(
            text = { Text("Take Photo") },
            onClick = { //onOptionSelected(AttachmentOption.TAKE_PHOTO)
                      },
            ///leadingIcon = { Icon(Icons.Filled.CameraAlt, contentDescription = "Take Photo") }
        )

    }
}



@Composable
fun WorklistItemUI(
    worklistItem: WorklistItem,
    onWorklistItemChange: (WorklistItem) -> Unit,
    onDeleteWorklistItem: () -> Unit // Callback to delete this worklist item
) {
    var internalWorklistItemName by remember(worklistItem.name) { mutableStateOf(worklistItem.name) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = internalWorklistItemName,
                onValueChange = { internalWorklistItemName = it },
                label = { Text("List Name") },
                modifier = Modifier.weight(1f),
                trailingIcon = { // Save button for list name
                    IconButton(onClick = {
                        onWorklistItemChange(worklistItem.copy(name = internalWorklistItemName))
                    }, enabled = internalWorklistItemName != worklistItem.name) {
                        Icon(Icons.Filled.Check, contentDescription = "Save list name")
                    }
                }
            )
            IconButton(onClick = {
                onWorklistItemChange(worklistItem.copy(isCollapsed = !worklistItem.isCollapsed))
            }) {
                Icon(
                    if (worklistItem.isCollapsed) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                    contentDescription = if (worklistItem.isCollapsed) "Expand" else "Collapse"
                )
            }
            IconButton(onClick = onDeleteWorklistItem) { // Delete button for the whole list
                Icon(Icons.Filled.Delete, contentDescription = "Delete this task list")
            }
        }

        if (!worklistItem.isCollapsed) {
            Spacer(modifier = Modifier.height(8.dp))
            worklistItem.tasks.forEachIndexed { taskIndex, task ->
                TaskItemUI(
                    task = task,
                    onTaskChange = { updatedTask ->
                        val newTasks = worklistItem.tasks.toMutableList()
                        newTasks[taskIndex] = updatedTask
                        onWorklistItemChange(worklistItem.copy(tasks = newTasks))
                    },
                    onDeleteTask = {
                        val newTasks = worklistItem.tasks.filterNot { it.id == task.id }
                        onWorklistItemChange(worklistItem.copy(tasks = newTasks))
                    }
                )
            }
            TextButton(
                onClick = {
                    val newTask = Task(text = "New Task", isCompleted = false)
                    onWorklistItemChange(worklistItem.copy(tasks = worklistItem.tasks + newTask))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add task")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Task")
            }
        }
    }
}

@Composable
fun TaskItemUI(
    task: Task,
    onTaskChange: (Task) -> Unit,
    onDeleteTask: () -> Unit
) {
    var internalTaskText by remember(task.text) { mutableStateOf(task.text) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = { onTaskChange(task.copy(isCompleted = it)) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedTextField(
            value = internalTaskText,
            onValueChange = { internalTaskText = it },
            modifier = Modifier.weight(1f),
            label = {Text("Task")},
            singleLine = true, // Or remove if tasks can be multi-line
            trailingIcon = {
                IconButton(onClick = {
                    onTaskChange(task.copy(text = internalTaskText))
                }, enabled = internalTaskText != task.text ) { // Enable only if changed
                    Icon(Icons.Filled.Check, contentDescription = "Save task text")
                }
            }
        )
        IconButton(onClick = onDeleteTask) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete task")
        }
    }
}
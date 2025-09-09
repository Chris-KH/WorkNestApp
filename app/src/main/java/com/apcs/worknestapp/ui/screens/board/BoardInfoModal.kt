package com.apcs.worknestapp.ui.screens.board

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.board.Board
import com.apcs.worknestapp.data.remote.board.BoardViewModel
import com.apcs.worknestapp.domain.usecase.AppDefault
import com.apcs.worknestapp.ui.components.CustomSnackBar
import com.apcs.worknestapp.ui.components.inputfield.CustomTextField
import com.apcs.worknestapp.ui.theme.Roboto
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardInfoModal(
    board: Board,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    boardViewModel: BoardViewModel,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val modalSnackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden }
    )
    val madeUser = board.members.find { it.docId == board.ownerId }
    var editableBoardName by remember(board.name) { mutableStateOf(board.name ?: "") }
    var editableBoardDescription by remember(board.description) {
        mutableStateOf(board.description ?: "")
    }

    fun clearFocus() {
        focusRequester.requestFocus()
        focusRequester.freeFocus()
        focusManager.clearFocus()
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
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(1000f)
            ) { CustomSnackBar(data = it) }
            Column(
                modifier = Modifier
                    .clickable(
                        onClick = { clearFocus() },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .fillMaxSize()
            ) {
                val labelTextStyle = TextStyle(
                    fontSize = 12.sp, lineHeight = 12.sp, letterSpacing = (0).sp,
                    fontWeight = FontWeight.Normal, fontFamily = Roboto,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val contentTextStyle = TextStyle(
                    fontSize = 16.sp, lineHeight = 20.sp, letterSpacing = (0).sp,
                    fontWeight = FontWeight.Normal, fontFamily = Roboto,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(vertical = 6.dp),
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
                        text = "About this board",
                        modifier = Modifier.align(alignment = Alignment.Center)
                    )
                }
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 32.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    item(key = "Board Name") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "BOARD NAME",
                                style = labelTextStyle,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            CustomTextField(
                                value = editableBoardName,
                                onValueChange = { editableBoardName = it },
                                textStyle = contentTextStyle,
                                singleLine = true,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        coroutineScope.launch {
                                            val initialName = board.name ?: ""
                                            val boardId = board.docId
                                            val newName = editableBoardName
                                            if (newName.isNotBlank() && newName != initialName && boardId != null) {
                                                val message = boardViewModel.updateBoardName(
                                                    boardId, newName
                                                )
                                                clearFocus()
                                                if (message != null) {
                                                    editableBoardName = initialName
                                                    modalSnackbarHostState.showSnackbar(
                                                        message = message,
                                                        withDismissAction = true,
                                                    )
                                                }
                                            } else clearFocus()
                                        }
                                    }
                                ),
                                contentPadding = PaddingValues(
                                    vertical = 12.dp,
                                    horizontal = 16.dp
                                ),
                                modifier = Modifier
                                    .onFocusChanged {
                                        val isFocused = it.isFocused
                                        if (!isFocused) {
                                            coroutineScope.launch {
                                                val initialName = board.name ?: ""
                                                val boardId = board.docId
                                                val newName = editableBoardName
                                                if (newName.isNotBlank() && newName != initialName && boardId != null) {
                                                    val message = boardViewModel.updateBoardName(
                                                        boardId, newName
                                                    )
                                                    clearFocus()
                                                    if (message != null) {
                                                        editableBoardName = initialName
                                                        modalSnackbarHostState.showSnackbar(
                                                            message = message,
                                                            withDismissAction = true,
                                                        )
                                                    }
                                                } else clearFocus()
                                            }
                                        }
                                    }
                                    .fillMaxWidth()
                            )
                        }
                    }

                    if (madeUser != null) {
                        item { Spacer(modifier = Modifier.height(32.dp)) }

                        item(key = "Made By") {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "MADE BY",
                                    style = labelTextStyle,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                        .padding(vertical = 10.dp, horizontal = 16.dp)
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context).data(madeUser.avatar)
                                            .crossfade(true).build(),
                                        placeholder = painterResource(R.drawable.fade_avatar_fallback),
                                        error = painterResource(R.drawable.fade_avatar_fallback),
                                        contentDescription = "Avatar",
                                        contentScale = ContentScale.Crop,
                                        filterQuality = FilterQuality.Medium,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .aspectRatio(1f)
                                            .clip(CircleShape),
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    ) {
                                        Text(
                                            text = madeUser.name ?: AppDefault.USER_NAME,
                                            fontSize = 15.sp,
                                            lineHeight = 15.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = madeUser.email ?: "",
                                            fontSize = 13.sp,
                                            lineHeight = 13.sp,
                                            fontWeight = FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(32.dp)) }

                    item(key = "Board Description") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "DESCRIPTION",
                                style = labelTextStyle,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            CustomTextField(
                                value = editableBoardDescription,
                                onValueChange = { editableBoardDescription = it },
                                placeholder = {
                                    Text(
                                        text = "It's your board's time to shine! " +
                                                "Let people know what this board is used " +
                                                "for and what they can expect to see",
                                        style = contentTextStyle,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                                textStyle = contentTextStyle,
                                singleLine = true,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        coroutineScope.launch {
                                            val initialDescription = board.description ?: ""
                                            val boardId = board.docId
                                            val newDescription = editableBoardDescription
                                            if (newDescription != initialDescription && boardId != null) {
                                                val message = boardViewModel.updateBoardDescription(
                                                    boardId, newDescription
                                                )
                                                clearFocus()
                                                if (message != null) {
                                                    editableBoardDescription = initialDescription
                                                    modalSnackbarHostState.showSnackbar(
                                                        message = message,
                                                        withDismissAction = true,
                                                    )
                                                }
                                            } else clearFocus()
                                        }
                                    }
                                ),
                                contentPadding = PaddingValues(
                                    vertical = 12.dp,
                                    horizontal = 16.dp
                                ),
                                modifier = Modifier
                                    .onFocusChanged {
                                        val isFocused = it.isFocused
                                        if (!isFocused) {
                                            coroutineScope.launch {
                                                val initialDescription = board.description ?: ""
                                                val boardId = board.docId
                                                val newDescription = editableBoardDescription
                                                if (newDescription != initialDescription && boardId != null) {
                                                    val message =
                                                        boardViewModel.updateBoardDescription(
                                                            boardId, newDescription
                                                        )
                                                    clearFocus()
                                                    if (message != null) {
                                                        editableBoardDescription =
                                                            initialDescription
                                                        modalSnackbarHostState.showSnackbar(
                                                            message = message,
                                                            withDismissAction = true,
                                                        )
                                                    }
                                                } else clearFocus()
                                            }
                                        }
                                    }
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

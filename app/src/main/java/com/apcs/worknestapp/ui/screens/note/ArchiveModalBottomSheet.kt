package com.apcs.worknestapp.ui.screens.note

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.note.Note
import com.apcs.worknestapp.ui.screens.Screen
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    navController: NavHostController,
    archiveNotes: List<Note>,
    onRestore: (List<String>) -> Unit,
) {
    var isInSelectMode by remember { mutableStateOf(false) }
    val selectedNotes = remember { mutableStateListOf<String>() }
    val lazyListState = rememberLazyListState()
    val isScrolled = lazyListState.canScrollBackward

    LaunchedEffect(isInSelectMode) {
        if (!isInSelectMode) selectedNotes.clear()
    }

    LaunchedEffect(archiveNotes) {
        if (isInSelectMode) {
            selectedNotes.removeAll { noteId ->
                archiveNotes.find { it.docId == noteId } == null
            }
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(12.dp),
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxSize()
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedContent(
                targetState = isInSelectMode,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isScrolled) MaterialTheme.colorScheme.surfaceContainerHighest
                            else MaterialTheme.colorScheme.surface
                        )
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!it) {
                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close modal",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Text(text = "Archive")
                        IconButton(
                            onClick = { isInSelectMode = true },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_select),
                                contentDescription = "Select mode",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        TextButton(
                            onClick = { isInSelectMode = false },
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Text(text = "Done")
                        }
                        Text(text = "${selectedNotes.size} Selected")
                        TextButton(
                            enabled = selectedNotes.isNotEmpty(),
                            onClick = {
                                onRestore(selectedNotes.toList())
                            },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Text(text = "Restore")
                        }
                    }
                }
            }

            LazyColumn(
                state = lazyListState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 12.dp)
            ) {
                itemsIndexed(
                    items = archiveNotes,
                    key = { _, item -> item.docId ?: UUID.randomUUID() }
                ) { idx, note ->
                    val isSelected = selectedNotes
                        .find { it == note.docId } != null

                    NoteItem(
                        note = note,
                        selectedMode = isInSelectMode,
                        isSelected = isSelected,
                        onClick = {
                            if (note.docId == null) return@NoteItem
                            if (!isInSelectMode) {
                                navController.navigate(
                                    Screen.NoteDetail.route.replace(
                                        "{noteId}", note.docId
                                    )
                                )
                            } else {
                                if (!isSelected) selectedNotes.add(note.docId)
                                else selectedNotes.removeIf { it == note.docId }
                            }
                        },
                        onCompleteClick = {},
                        onLongClick = null,
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

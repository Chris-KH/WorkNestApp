package com.apcs.worknestapp.ui.screens.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apcs.worknestapp.data.remote.board.Board
import com.apcs.worknestapp.data.remote.board.BoardViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardMemberModal(
    board: Board,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    snackbarHost: SnackbarHostState,
    boardViewModel: BoardViewModel,
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = {
            it != SheetValue.Hidden
        }
    )

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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
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
                    text = "Manage board members",
                    modifier = Modifier.align(alignment = Alignment.Center)
                )
            }
        }
    }
}

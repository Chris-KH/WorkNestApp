package com.apcs.worknestapp.ui.screens.contact

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ContactSubScreen(
    currentSubScreen: ContactSubScreenState,
    listState: LazyListState,
    previousIndex: MutableIntState,
    onScroll: (topNavigationVisible: Boolean, previousIndex: Int) -> Unit,
) {
    LaunchedEffect(Unit) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { currentIndex ->
                if (currentIndex == previousIndex.intValue) return@collect
                if (currentIndex + 1 < previousIndex.intValue || currentIndex == 0) {
                    onScroll(true, currentIndex)
                } else if (currentIndex > previousIndex.intValue) {
                    onScroll(false, currentIndex)
                }
            }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
    ) {
        items(50) { item ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(8.dp)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text("Item $item", color = Color.White)
            }
        }

        item { Spacer(modifier = Modifier.height(60.dp)) }
    }
}

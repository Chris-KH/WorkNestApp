package com.apcs.worknestapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.ui.components.notedetail.CoverPreviewCard
import com.apcs.worknestapp.ui.components.notedetail.NoteModalBottomTopBar
import com.apcs.worknestapp.utils.ColorUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoverPickerModal(
    modifier: Modifier = Modifier,
    currentColor: Color? = null,
    onSave: (Color?) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedColor by remember { mutableStateOf(currentColor) }
    val listCoverColor = ColorUtils.listCoverColor
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = {
            it != SheetValue.Hidden
        }
    )

    ModalBottomSheet(
        sheetState = sheetState,
        dragHandle = null,
        shape = RoundedCornerShape(12.dp),
        onDismissRequest = {
            coroutineScope.launch {
                sheetState.hide()
                onDismissRequest()
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
            .fillMaxSize()
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
    ) {
        NoteModalBottomTopBar(
            title = "Cover Settings",
            onClose = {
                coroutineScope.launch {
                    sheetState.hide()
                    onDismissRequest()
                }
            },
            onSave = { onSave(selectedColor) },
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 8.dp)
        ) {
            val horizontalPadding = 6.dp

            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Preview",
                    fontSize = 16.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = horizontalPadding)
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                val shape = RoundedCornerShape(6.dp)
                CoverPreviewCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f)
                        .padding(vertical = 4.dp, horizontal = horizontalPadding)
                        .background(color = selectedColor ?: Color.DarkGray, shape = shape)
                        .padding(vertical = 8.dp, horizontal = horizontalPadding)
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(24.dp))
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Colors",
                    fontSize = 16.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(
                        vertical = 12.dp,
                        horizontal = horizontalPadding,
                    )
                )
            }

            itemsIndexed(
                items = listCoverColor, key = { idx, _ -> idx }) { index, color ->
                val isSelected = color == selectedColor
                val shape = RoundedCornerShape(4.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1.5f)
                        .padding(vertical = 4.dp, horizontal = horizontalPadding)
                        .border(
                            width = 2.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else Color.Transparent,
                            shape = shape,
                        )
                        .background(color = color, shape = shape)
                        .clip(shape)
                        .clickable(onClick = { selectedColor = color })
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                TextButton(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(vertical = 24.dp),
                    onClick = { selectedColor = null },
                ) {
                    Text(
                        text = "Remove cover",
                        fontSize = 16.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(
                            vertical = 8.dp,
                            horizontal = horizontalPadding,
                        )
                    )
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

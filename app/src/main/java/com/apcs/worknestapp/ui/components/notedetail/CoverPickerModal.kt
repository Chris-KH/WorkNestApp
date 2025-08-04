package com.apcs.worknestapp.ui.components.notedetail

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoverPickerModal(
    currentColor: Color? = null,
    onSave: (Color?) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedColor by remember { mutableStateOf<Color?>(currentColor) }

    val listCoverColor: List<Color> = listOf(
        Color(0xFF366B4D),
        Color(0xFF79611B),
        Color(0xFF8C531E),
        Color(0xFFA13825),
        Color(0xFF5B4CAB),
        Color(0xFF2453C7),
        Color(0xFF356A70),
        Color(0xFF53692B),
        Color(0xFF8C4273),
        Color(0xFF5B6473),
        Color(0xFF304946),
        Color(0xFF484B52),
        Color(0xFF507A86),
        Color(0xFF5A4f4B),
        Color(0xFF737278),
    )

    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        ),
        dragHandle = null,
        shape = RoundedCornerShape(12.dp),
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
            .fillMaxSize()
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
    ) {
        NoteModalBottomTopBar(
            title = "Cover Settings",
            onClose = onDismissRequest,
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
                ColorPreviewCard(
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
                items = listCoverColor,
                key = { idx, _ -> idx }
            ) { index, color ->
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
        }
    }
}

package com.apcs.worknestapp.ui.screens.editprofile

import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.SubcomposeLayout

@Composable
fun EditItemColumn(
    items: List<Pair<String, String?>>,
    onItemClick: (field: String) -> Unit,
) {
    SubcomposeLayout { constraints ->
        val labelPlaceables = items.mapIndexed { index, (label, _) ->
            subcompose("label$index") {
                EditItemLabel(label = label)
            }.first().measure(constraints)
        }

        val maxLabelWidth = labelPlaceables.maxOf { it.width }

        val rowPlaceables = items.mapIndexed { index, (label, value) ->
            subcompose("row$index") {
                EditItemRow(
                    label = label,
                    value = value ?: "",
                    labelWidth = maxLabelWidth,
                    onClick = { onItemClick(label) }
                )
            }.first().measure(constraints)
        }

        val totalHeight = rowPlaceables.sumOf { it.height }

        layout(constraints.maxWidth, totalHeight) {
            var y = 0
            rowPlaceables.forEach { placeable ->
                placeable.place(0, y)
                y += placeable.height
            }
        }
    }
}

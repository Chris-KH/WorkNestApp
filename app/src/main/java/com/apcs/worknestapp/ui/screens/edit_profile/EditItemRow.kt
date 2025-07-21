package com.apcs.worknestapp.ui.screens.edit_profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.theme.Inter
import com.apcs.worknestapp.ui.theme.Roboto

@Composable
fun EditItemRow(
    label: String,
    value: String,
    labelWidth: Int, // Pixel
    isLastItem: Boolean,
    onClick: () -> Unit,
) {
    val labelDp = with(LocalDensity.current) { labelWidth.toDp() }

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.End,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp,
                    vertical = 12.dp
                ),
        ) {
            EditItemLabel(
                label = label,
                modifier = Modifier.width(labelDp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (value.isNotBlank()) value else label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = Roboto,
                lineHeight = 18.sp,
                letterSpacing = 0.sp,
                color =
                    if (value.isNotBlank()) MaterialTheme.colorScheme.onBackground
                    else MaterialTheme.colorScheme.outline,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painter = painterResource(R.drawable.symbol_angle_arrow),
                contentDescription = "Go edit",
                modifier = Modifier
                    .size(20.dp)
                    .rotate(-90f)
            )
        }
        if (!isLastItem) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = labelDp + 8.dp + 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = (0.5).dp,
            )
        }
    }
}

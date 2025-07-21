package com.apcs.worknestapp.ui.screens.edit_profile

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.ui.theme.Inter

@Composable
fun EditItemLabel(
    label: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = Inter,
        lineHeight = 18.sp,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier,
    )
}

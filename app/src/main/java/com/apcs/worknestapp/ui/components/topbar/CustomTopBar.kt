package com.apcs.worknestapp.ui.components.topbar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.ui.theme.Roboto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar(
    field: String,
    showDivider: Boolean = true,
    navigationIcon: @Composable (() -> Unit) = {},
    actions: @Composable (RowScope.() -> Unit) = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = field,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Roboto,
                    fontSize = 16.sp,
                    lineHeight = 16.sp,
                    letterSpacing = (0).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                )
            },
            navigationIcon = navigationIcon,
            actions = actions,
            expandedHeight = TopBarDefault.expandedHeight,
            colors = colors,
            modifier = Modifier
        )

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = (1).dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}

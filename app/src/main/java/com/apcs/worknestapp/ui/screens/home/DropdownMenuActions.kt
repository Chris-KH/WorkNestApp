package com.apcs.worknestapp.ui.screens.home

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.theme.Roboto

@Composable
fun DropdownMenuActions(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onCreateBoard: () -> Unit,
    onCreateCard: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 32.dp,
        shape = RoundedCornerShape(25f),
        modifier = Modifier.widthIn(min = 200.dp),
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    text = "Create a board",
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    fontFamily = Roboto,
                    fontWeight = FontWeight.Normal,
                )
            },
            trailingIcon = {
                Icon(
                    painter = painterResource(R.drawable.outline_board),
                    contentDescription = "Create a board",
                    modifier = Modifier.size(24.dp),
                )
            },
            onClick = onCreateBoard,
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = {
                Text(
                    text = "Create a card",
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    fontFamily = Roboto,
                    fontWeight = FontWeight.Normal,
                )
            },
            trailingIcon = {
                Icon(
                    painter = painterResource(R.drawable.outline_card),
                    contentDescription = "Create a board",
                    modifier = Modifier.size(24.dp),
                )
            },
            onClick = onCreateCard,
        )
    }
}

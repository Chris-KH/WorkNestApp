package com.apcs.worknestapp.ui.screens.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.ui.theme.Roboto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationModalBottom(
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = {
            it != SheetValue.Hidden
        }
    )
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(12.dp),
        contentWindowInsets = { WindowInsets(0.dp) },
        dragHandle = {
            Box(
                modifier = Modifier
                    .background(
                        color =
                            if (scrollState.value == 0)
                                MaterialTheme.colorScheme.surfaceContainerLow
                            else MaterialTheme.colorScheme.surface
                    )
                    .padding(top = 8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(height = 4.dp, width = 32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = RoundedCornerShape(28.dp)
                        )
                        .align(alignment = Alignment.TopCenter)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                ) {
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close modal",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "Notification Settings",
                        fontSize = 15.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Roboto,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        },
    ) {
        val horizontalPadding = 16.dp

        Column(
            modifier = Modifier
                .fillMaxHeight(0.5f)
                .verticalScroll(scrollState)
                .fillMaxWidth()
                .padding(top = 20.dp)
        ) {
            Text(
                text = "ON MOBILE, NOTIFY ME ABOUT...",
                fontSize = 12.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = Roboto,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
            )
            Spacer(modifier = Modifier.height(8.dp))

            repeat(50) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surface
                        )
                        .padding(horizontal = horizontalPadding, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Test $it",
                        fontSize = 14.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = Roboto,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(
                modifier = Modifier.height(
                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                )
            )
        }
    }
}

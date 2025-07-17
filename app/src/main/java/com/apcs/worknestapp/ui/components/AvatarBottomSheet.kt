package com.apcs.worknestapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.theme.Inter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarBottomSheet(
    onDismiss: () -> Unit,
    onChooseFromLibrary: () -> Unit,
    onTakePhoto: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Choose avatar",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = Inter,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
            )

            ListItem(
                headlineContent = {
                    Text(
                        text = "Choose from library",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = Inter,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.outline_photo),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier.clickable {
                    onChooseFromLibrary()
                    onDismiss()
                },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    headlineColor = MaterialTheme.colorScheme.onSurface,
                    leadingIconColor = MaterialTheme.colorScheme.onSurface,
                )
            )

            ListItem(
                headlineContent = {
                    Text(
                        text = "Take photo",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = Inter,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.outline_camera),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier.clickable {
                    onTakePhoto()
                    onDismiss()
                },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    headlineColor = MaterialTheme.colorScheme.onSurface,
                    leadingIconColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        }
    }
}

package com.apcs.worknestapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.theme.Roboto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarBottomSheet(
    onDismiss: () -> Unit,
    onChooseFromLibrary: () -> Unit,
    onTakePhoto: () -> Unit,
    onViewAvatar: (() -> Unit)? = null,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        val listItemColor = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            headlineColor = MaterialTheme.colorScheme.onSurface,
            leadingIconColor = MaterialTheme.colorScheme.onSurface,
            trailingIconColor = MaterialTheme.colorScheme.onSurface,
        )
        val headlineTextStyle = TextStyle(
            fontSize = 14.sp, lineHeight = 14.sp, letterSpacing = 0.sp,
            fontFamily = Roboto, fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Choose avatar",
                fontSize = 16.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Roboto,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            )

            if (onViewAvatar != null) {
                ListItem(
                    headlineContent = { Text(text = "View avatar", style = headlineTextStyle) },
                    leadingContent = {
                        IconWithBackground(
                            painter = painterResource(R.drawable.outline_profile),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    modifier = Modifier.clickable {
                        onViewAvatar()
                        onDismiss()
                    },
                    colors = listItemColor
                )
            }

            ListItem(
                headlineContent = { Text(text = "Choose from library", style = headlineTextStyle) },
                leadingContent = {
                    IconWithBackground(
                        painter = painterResource(R.drawable.outline_photo),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier.clickable {
                    onChooseFromLibrary()
                    onDismiss()
                },
                colors = listItemColor
            )

            ListItem(
                headlineContent = { Text(text = "Take photo", style = headlineTextStyle) },
                leadingContent = {
                    IconWithBackground(
                        painter = painterResource(R.drawable.outline_camera),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier.clickable {
                    onTakePhoto()
                    onDismiss()
                },
                colors = listItemColor
            )
        }
    }
}

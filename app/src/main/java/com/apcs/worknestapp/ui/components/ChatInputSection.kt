package com.apcs.worknestapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.LocalAuthViewModel
import com.apcs.worknestapp.ui.components.inputfield.CustomTextField
import com.apcs.worknestapp.ui.theme.Roboto
import com.apcs.worknestapp.R

@Composable
fun ChatInputSection(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
    ) {
        HorizontalDivider(
            modifier = Modifier.align(alignment = Alignment.TopCenter)
        )
        Row(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {}
            ) {
                Icon(
                    painter = painterResource(R.drawable.fill_camera),
                    contentDescription = "Camera",
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(
                onClick = {}
            ) {
                Icon(
                    painter = painterResource(R.drawable.fill_image),
                    contentDescription = "Camera",
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            CustomTextField(
                value = text,
                onValueChange = onTextChange,
                textStyle = TextStyle(
                    fontSize = 15.sp, lineHeight = 15.sp, letterSpacing = 0.sp,
                    fontFamily = Roboto, fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                placeholder = {
                    Text(
                        text = "Aaa",
                        fontSize = 15.sp, lineHeight = 15.sp, letterSpacing = 0.sp,
                        fontFamily = Roboto, fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 12.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = (0.8).dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                ),
                maxLines = 4
            )
            IconButton(
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = Color.Unspecified,
                ),
                onClick = {
                    onSend()
                },
                enabled = text.isNotBlank()
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, "Post comment")
            }
        }
    }
}

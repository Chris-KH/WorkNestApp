package com.apcs.worknestapp.ui.screens.note

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
fun NoteItemDialog(
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.fillMaxWidth(0.8f)
    ) {
        val buttonTextStyle = TextStyle(
            fontSize = 15.sp, lineHeight = 15.sp, letterSpacing = 0.sp,
            fontFamily = Roboto, fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                val rowPadding = PaddingValues(vertical = 16.dp, horizontal = 20.dp)
                val leadingIconSize = 24.dp
                val leadingIconSpacer = 12.dp
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onArchive)
                        .padding(rowPadding)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_store),
                        contentDescription = "Archive note",
                        modifier = Modifier.size(leadingIconSize)
                    )
                    Spacer(modifier = Modifier.width(leadingIconSpacer))
                    Text(text = "Archive", style = buttonTextStyle)
                }
                HorizontalDivider()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onDelete)
                        .padding(rowPadding)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_trash),
                        contentDescription = "Delete note",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(leadingIconSize)
                    )
                    Spacer(modifier = Modifier.width(leadingIconSpacer))
                    Text(
                        text = "Delete note",
                        style = buttonTextStyle,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

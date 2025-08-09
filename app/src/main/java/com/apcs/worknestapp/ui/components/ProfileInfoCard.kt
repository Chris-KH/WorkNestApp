package com.apcs.worknestapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.domain.logic.DateFormater
import com.apcs.worknestapp.ui.theme.NotoSerif
import com.apcs.worknestapp.ui.theme.Roboto
import com.google.firebase.Timestamp

@Composable
fun ProfileInfoCard(
    bio: String?,
    createdAt: Timestamp?,
    modifier: Modifier = Modifier,
) {
    val labelStyle = TextStyle(
        fontFamily = NotoSerif, fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp, lineHeight = 15.sp, letterSpacing = 0.sp,
    )

    val contentStyle = TextStyle(
        fontFamily = Roboto, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 15.sp, letterSpacing = 0.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "About me", style = labelStyle)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (bio.isNullOrEmpty()) "..." else bio,
                style = contentStyle,
                textAlign = TextAlign.Justify,
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Joined since", style = labelStyle)
            Text(
                text = if (createdAt != null) DateFormater.format(createdAt.toDate()) else "No information",
                style = contentStyle,
            )
        }
    }
}

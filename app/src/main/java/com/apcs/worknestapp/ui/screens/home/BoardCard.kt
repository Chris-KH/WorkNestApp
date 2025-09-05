package com.apcs.worknestapp.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.data.remote.board.Board
import com.apcs.worknestapp.ui.components.RotatingIcon
import com.apcs.worknestapp.ui.theme.Roboto
import com.apcs.worknestapp.utils.ColorUtils
import com.apcs.worknestapp.R

@Composable
fun BoardCard(
    board: Board,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val boardCoverColor = board.cover?.let { ColorUtils.safeParse(it) }

    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(32.dp)
                .aspectRatio(1.3f)
                .background(
                    color = boardCoverColor ?: Color.Gray,
                    shape = RoundedCornerShape(6.dp)
                )
        ) {
            if (board.isLoading == true) {
                RotatingIcon(
                    painter = painterResource(R.drawable.loading_icon_4),
                    contentDescription = "Creating board",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = board.name ?: "Untitled Board",
            fontSize = 15.sp,
            lineHeight = 15.sp,
            fontFamily = Roboto,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Normal,
        )
    }
}

package com.apcs.worknestapp.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.components.inputfield.SearchInput
import com.apcs.worknestapp.ui.theme.Roboto
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeMainScreen(
    modifier: Modifier = Modifier,
    onNavigateToWorkspace: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    fun refresh() {
        coroutineScope.launch {
            isRefreshing = true
            delay(3000)
            isRefreshing = false
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { refresh() },
        modifier = modifier.fillMaxSize()
    ) {
        var searchValue by remember { mutableStateOf("") }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            val horizontalPadding = 16.dp

            item {
                var noteValue by remember { mutableStateOf("") }

                SearchInput(
                    value = searchValue,
                    onValueChange = { searchValue = it },
                    onCancel = { focusManager.clearFocus() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding, vertical = 12.dp),
                    interactionSource = remember { MutableInteractionSource() }
                )

                QuickAddNoteInput(
                    value = noteValue,
                    onValueChange = { noteValue = it },
                    onCancel = { focusManager.clearFocus() },
                    onAdd = {},
                    interactionSource = remember { MutableInteractionSource() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding, vertical = 10.dp)
                )

                Text(
                    text = "Your workspaces".uppercase(),
                    fontSize = 16.sp,
                    lineHeight = 16.sp,
                    fontFamily = Roboto,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding, vertical = 12.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(onClick = onNavigateToWorkspace)
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding, vertical = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_workspace),
                        contentDescription = "Workspace",
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Workspace Boards",
                        fontSize = 14.sp,
                        lineHeight = 14.sp,
                        fontFamily = Roboto,
                        fontWeight = FontWeight.Normal,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Boards",
                            fontSize = 14.sp,
                            lineHeight = 14.sp,
                            fontFamily = Roboto,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.rotate(90f),
                        )
                    }
                }
                HorizontalDivider()
            }

            items(count = 50) {
                Row(
                    modifier = Modifier
                        .clickable(onClick = {})
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = horizontalPadding, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .aspectRatio(1.33f)
                            .background(
                                color = Color.Gray,
                                shape = RoundedCornerShape(6.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(horizontalPadding))
                    Text(
                        text = "Hello",
                        fontSize = 15.sp,
                        lineHeight = 15.sp,
                        fontFamily = Roboto,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Normal,
                    )
                }
                HorizontalDivider()
            }
        }
    }
}

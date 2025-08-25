package com.apcs.worknestapp.ui.components.topbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.apcs.worknestapp.R
import com.apcs.worknestapp.ui.components.inputfield.SearchInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    value: String,
    onValueChange: (String) -> Unit,
    onCancel: () -> Unit,
    navController: NavHostController,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val animationDuration = 300

    Column(modifier = Modifier.fillMaxWidth()) {
        TopAppBar(
            title = {
                SearchInput(
                    value = value,
                    onValueChange = onValueChange,
                    onCancel = onCancel,
                    interactionSource = interactionSource,
                    animationDuration = animationDuration,
                    shape = RoundedCornerShape(50f),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                )
            },
            navigationIcon = {
                val isFocused by interactionSource.collectIsFocusedAsState()
                AnimatedVisibility(
                    visible = !isFocused,
                    enter = slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(animationDuration),
                    ) + expandHorizontally(
                        animationSpec = tween(animationDuration),
                    ),
                    exit = slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(animationDuration),
                    ) + shrinkHorizontally(
                        animationSpec = tween(animationDuration),
                    ),
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(R.drawable.symbol_angle_arrow),
                            contentDescription = "back",
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(90f)
                        )
                    }
                }
            },
            actions = {},
            expandedHeight = TopBarDefault.expandedHeight,
            colors = colors,
            modifier = Modifier
        )

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = (1).dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

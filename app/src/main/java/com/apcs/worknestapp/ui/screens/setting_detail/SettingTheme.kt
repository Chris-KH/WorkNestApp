package com.apcs.worknestapp.ui.screens.setting_detail

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.local.theme.ThemeMode
import com.apcs.worknestapp.data.local.theme.ThemeViewModel

@Composable
fun SettingTheme(
    modifier: Modifier = Modifier,
    themeViewModel: ThemeViewModel = hiltViewModel(),
) {
    val themeState = themeViewModel.theme.collectAsState()
    val themeMode = themeState.value

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 20.dp, horizontal = 16.dp)
        ) {
            item {
                Text(text = "Choose theme")
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(25f))
                ) {
                    ThemeOption(
                        label = "System",
                        isSelected = themeMode == ThemeMode.SYSTEM,
                        leadingIcon = R.drawable.outline_system,
                        onClick = {
                            themeViewModel.saveTheme(ThemeMode.SYSTEM)
                        }
                    )
                    HorizontalDivider(
                        thickness = (0.7).dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    ThemeOption(
                        label = "Light",
                        isSelected = themeMode == ThemeMode.LIGHT,
                        leadingIcon = R.drawable.outline_sun,
                        onClick = {
                            themeViewModel.saveTheme(ThemeMode.LIGHT)
                        }
                    )
                    HorizontalDivider(
                        thickness = (0.7).dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    ThemeOption(
                        label = "Dark",
                        isSelected = themeMode == ThemeMode.DARK,
                        leadingIcon = R.drawable.outline_moon,
                        onClick = {
                            themeViewModel.saveTheme(ThemeMode.DARK)
                        }
                    )
                }
            }

        }
    }
}

@Composable
fun ThemeOption(
    label: String,
    isSelected: Boolean,
    @DrawableRes leadingIcon: Int,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            .clickable(onClick = {
                if (!isSelected) onClick()
            })
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
            )
            .padding(
                vertical = 16.dp, horizontal = 16.dp
            )
    ) {
        Icon(
            painter = painterResource(leadingIcon),
            contentDescription = "$label theme",
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )
        Icon(
            painter = painterResource(
                if (isSelected) R.drawable.fill_checkbox
                else R.drawable.outline_circle
            ),
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
    }
}

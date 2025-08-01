package com.apcs.worknestapp.ui.screens.setting_detail

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.local.language.LanguageMode
import com.apcs.worknestapp.data.local.language.LanguageViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingLanguage(
    snackbarHost: SnackbarHostState,
    modifier: Modifier = Modifier,
    languageViewModel: LanguageViewModel = hiltViewModel(),
) {
    val languageState = languageViewModel.language.collectAsState()
    val languageMode = languageState.value

    val coroutineScope = rememberCoroutineScope()

    fun notSupportNotify() {
        coroutineScope.launch {
            snackbarHost.showSnackbar(
                message = "Change language failed. Language does not supported",
                withDismissAction = true,
            )
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp)
        ) {
            LanguageOption(
                flag = R.drawable.flag_us,
                language = "English, US",
                isSelected = languageMode == LanguageMode.EN_US,
                onClick = {
                    languageViewModel.saveLanguage(LanguageMode.EN_US)
                }
            )
            LanguageOption(
                flag = R.drawable.flag_uk,
                language = "English, UK",
                isSelected = languageMode == LanguageMode.EN_UK,
                onClick = {
                    languageViewModel.saveLanguage(LanguageMode.EN_UK)
                    notSupportNotify()
                },
            )
            LanguageOption(
                flag = R.drawable.flag_vietnam,
                language = "Vietnamese",
                isSelected = languageMode == LanguageMode.VN,
                onClick = {
                    languageViewModel.saveLanguage(LanguageMode.VN)
                    notSupportNotify()
                },
            )
            LanguageOption(
                flag = R.drawable.flag_china,
                language = "Chinese",
                isSelected = languageMode == LanguageMode.CN,
                onClick = {
                    languageViewModel.saveLanguage(LanguageMode.CN)
                    notSupportNotify()
                },
            )
        }
    }
}

@Composable
fun LanguageOption(
    @DrawableRes flag: Int,
    language: String,
    isSelected: Boolean,
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
        Image(
            painter = painterResource(flag),
            contentDescription = language,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = language,
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

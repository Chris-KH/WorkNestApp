package com.apcs.worknestapp.ui.screens.setting_detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingAbout(
    modifier: Modifier = Modifier,
) {
    //TODO: Description about the app
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 20.dp, horizontal = 16.dp)
        ) {
            item {
                Text(
                    text = "About",
                )
            }
        }
    }
}

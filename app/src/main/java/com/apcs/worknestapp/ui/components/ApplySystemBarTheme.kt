package com.apcs.worknestapp.ui.components

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat

@Composable
fun ApplySystemBarTheme(isDark: Boolean) {
    val activity = LocalActivity.current ?: return
    val useDarkIcons = !isDark
    SideEffect {
        val window = activity.window
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = useDarkIcons
        insetsController.isAppearanceLightNavigationBars = useDarkIcons
    }
}

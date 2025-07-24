package com.apcs.worknestapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    background = Color(0xFF0C0E10), //OK
    onBackground = Color(0xFFFFFFFF), //OK
    surface = Color(0xFF1C1E20), //OK
    surfaceContainer = Color(0xFF1E1F25), //OK
    surfaceContainerLow = Color(0xFF121416), //OK
    surfaceContainerLowest = Color(0xFF060A0F), //OK
    surfaceContainerHigh = Color(0xFF292A2E), //OK
    surfaceContainerHighest = Color(0xFF33343A), //OK
    surfaceVariant = Color(0xFF45464F),//OK
    onSurface = Color(0xFFFFFFFF),//OK
    primary = Color(0xFFB2C5FF), //OK
    onPrimary = Color(0xFF000000), //OK
    secondary = Color(0xFFC0C6DD),//OK
    secondaryContainer = Color(0xFF414659), //OK
    onSecondary = Color(0xFF000000), //OK
    error = Color(0xFFAA3D3D), //OK
    onError = Color(0xFFFFFFFF), //OK
    outline = Color(0xFF8f909a), //OK
    outlineVariant = Color(0xFF45464F) //OK
)

private val LightColorScheme = lightColorScheme(
    background = Color(0xFFF0F2F4),// OK
    onBackground = Color(0xFF000000), //OK
    surface = Color(0xFFFFFFFF), //OK
    surfaceContainer = Color(0xFFEDEFF4), //OK
    surfaceContainerLowest = Color(0xFFFFFFFF), //OK
    surfaceContainerHigh = Color(0xFFE3E6E8), //OK
    surfaceContainerHighest = Color(0xFFD8DBDE), //OK
    surfaceVariant = Color(0xFFE1E2EC),//OK
    onSurface = Color(0xFF000000), //OK
    primary = Color(0xFF495D92), //OK
    onPrimary = Color(0xFFFFFFFF), //OK
    secondary = Color(0xFF585E71),//OK
    secondaryContainer = Color(0xFFDDE2f9),//OK
    onSecondary = Color(0xFFFFFFFF),//OK
    error = Color(0xFFB00020), //OK
    onError = Color(0xFFFFFFFF), //OK
    outline = Color(0xFF757780), //OK
    outlineVariant = Color(0xFFC5C6D0) //OK
)

@Composable
fun WorkNestAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


/*
private val DarkColorScheme = darkColorScheme(
    background = Color(0xFF181818), //OK
    onBackground = Color(0xFFFFFFFF), //OK
    surface = Color(0xFF303030), // OK
    onSurface = Color(0xFFFFFFFF), //OK
    primary = Color(0xFFBC8AF9), //OK
    onPrimary = Color(0xFF000000), //OK
    secondary = Color(0xFF2E7D32), //OK
    onSecondary = Color(0xFFDFFFE2), //OK
    tertiary = Color(0xFFF9A825), //OK
    onTertiary = Color(0xFFFFF8E1), // OK
    error = Color(0xFFCF6679), //OK
    onError = Color(0xFFFFFFFF), //OK
)

private val LightColorScheme = lightColorScheme(
    background = Color(0xFFFFFFFF), // OK
    onBackground = Color(0xFF000000), //OK
    surface = Color(0xFFEEEEF1), //OK
    onSurface = Color(0xFF000000), //OK
    primary = Color(0xFF1D5797), //OK
    onPrimary = Color(0xFFFFFFFF), //OK
    secondary = Color(0xFF2C632C), //Ok
    onSecondary = Color(0xFFDFFFE2), //OK
    tertiary = Color(0xFFE3960E), //OK
    onTertiary = Color(0xFFFFF8E1), //OK
    error = Color(0xFFB00020), //OK
    onError = Color(0xFFFFFFFF), //OK
)
 */

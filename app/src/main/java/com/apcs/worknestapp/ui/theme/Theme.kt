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
    background = Color(red = 18, green = 18, blue = 18), //OK
    onBackground = Color(0xFFFFFFFF), //OK
    surface = Color(red = 32, green = 32, blue = 32), // OK
    surfaceVariant = Color(red = 62, green = 50, blue = 73, alpha = 255),
    onSurface = Color(0xFFFFFFFF), //OK
    primary = Color(0xFFBC8AF9), //OK
    onPrimary = Color(0xFF000000), //OK
    error = Color(0xFFAA3D3D), //OK
    onError = Color(0xFFFFFFFF), //OK
)

private val LightColorScheme = lightColorScheme(
    background = Color(red = 240, green = 240, blue = 240),// OK
    onBackground = Color(0xFF000000), //OK
    surface = Color(red = 255, green = 255, blue = 255), //OK
    surfaceVariant = Color(red = 228, green = 231, blue = 237, alpha = 255),
    onSurface = Color(0xFF000000), //OK
    primary = Color(0xFF145AB4), //OK
    onPrimary = Color(0xFFFFFFFF), //OK
    secondary = Color(0xFF2E76BB),
    secondaryContainer = Color(0xFFE1EEF6),
    onSecondary = Color(0xFFFFFFFF),
    error = Color(0xFFB00020), //OK
    onError = Color(0xFFFFFFFF), //OK
)

//    primary = Color(0xFF1D5797), //OK

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

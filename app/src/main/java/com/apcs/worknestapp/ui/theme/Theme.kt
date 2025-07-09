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
    background = Color(0xFF181818), //OK
    onBackground = Color(0xFFFFFFFF), //OK
    surface = Color(0xFF303030), // OK
    onSurface = Color(0xFFFFFFFF), //OK
    primary = Purple80, //TODO
    onPrimary = Purple80, //TODO
    secondary = Color(0xFF2E7D32), //OK
    onSecondary = Color(0xFFDFFFE2), //OK
    tertiary = Color(0xFFF9A825), //OK
    onTertiary = Color(0xFFFFF8E1), // OK
    error = Color(0xFFCF6679), //OK
    onError = Color(0xFFFFFFFF), //OK
    outline = Color(0xFFA6988A),
)

private val LightColorScheme = lightColorScheme(
    background = Color(0xFFFFFFFF), // OK
    onBackground = Color(0xFF000000), //TODO
    surface = Color(0xFFEEEEEE), //OK
    onSurface = Color(0xFF000000), //TODO
    primary = Purple40, //TODO
    onPrimary = Purple80, //TODO
    secondary = Color(0xFF1B3B1B), //Ok
    onSecondary = Color(0xFFDFFFE2), //OK
    tertiary = Color(0xFFE3960E), //OK
    onTertiary = Color(0xFFFFF8E1), //OK
    error = Color(0xFFB00020), //OK
    onError = Color(0xFFFFFFFF), //OK
    outline = Color(0xFFD9D0C2),
)

@Composable
fun WorkNestAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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

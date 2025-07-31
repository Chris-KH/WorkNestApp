package com.apcs.worknestapp.utils

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

object ColorUtils {
    fun safeParse(hex: String): Color? {
        return try {
            Color(hex.toColorInt())
        } catch(e: IllegalArgumentException) {
            null
        }
    }
}

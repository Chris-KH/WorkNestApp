package com.apcs.worknestapp.utils

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

object ColorUtils {
    fun safeParse(hex: Int): Color? {
        return try {
            Color(hex)
        } catch(e: IllegalArgumentException) {
            null
        }
    }
}

package com.apcs.worknestapp.utils

import androidx.compose.ui.graphics.Color

object ColorUtils {
    val listCoverColor: List<Color> = listOf(
        Color(0xFF366B4D),
        Color(0xFF79611B),
        Color(0xFF8C531E),
        Color(0xFFA13825),
        Color(0xFF5B4CAB),
        Color(0xFF2453C7),
        Color(0xFF356A70),
        Color(0xFF53692B),
        Color(0xFF8C4273),
        Color(0xFF5B6473),
        Color(0xFF304946),
        Color(0xFF484B52),
        Color(0xFF507A86),
        Color(0xFF5A4f4B),
        Color(0xFF737278),
        Color(0xFF0A3755),
        Color(0xFFFAC846),
        Color(0xFF8CB45F),
        Color(0xFF6E5A7D),
        Color(0xFF733223),
        Color(0xFF464646),
        Color(0xFFC86469),
        Color(0xFF0A509B),
        Color(0xFF00A0AA),
        Color(0xFFE19B3C),
        Color(0xFFAAA5A5),
        Color(0xFF191423),
        Color(0xFF283C3C),
        Color(0xFF3C0000),
        Color(0xFF2D2D1E),
        Color(0xFF5096C3),
        Color(0xFFAA96B4),
        Color(0xFFD7B428),
        Color(0xFF806365),
        Color(0xFF085811),
    )

    fun safeParse(hex: Int): Color? {
        return try {
            Color(hex)
        } catch(e: IllegalArgumentException) {
            null
        }
    }
}

package com.apcs.worknestapp.domain.logic

import com.github.pemistahl.lingua.api.Language
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder

object LanguageDetector {
    val detector = LanguageDetectorBuilder.fromLanguages(
        Language.ENGLISH,
        Language.VIETNAMESE,
        Language.CHINESE,
        Language.FRENCH,
        Language.GERMAN,
    ).build()
}

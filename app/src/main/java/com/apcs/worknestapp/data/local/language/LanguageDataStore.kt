package com.apcs.worknestapp.data.local.language

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.languageDataStore by preferencesDataStore(name = "language_prefs")

object LanguagePreferenceKeys {
    val LANGUAGE_KEY = stringPreferencesKey("language")
}

enum class LanguageMode {
    EN_US,
    EN_UK,
    VN,
    CN,
}

class LanguageDataStore @Inject constructor(
    private val context: Context,
) {
    val languageFlow: Flow<LanguageMode> = context.languageDataStore.data
        .map { preferences ->
            val value = preferences[LanguagePreferenceKeys.LANGUAGE_KEY]
            LanguageMode.entries.find { it.name == value } ?: LanguageMode.EN_US
        }

    suspend fun saveLanguage(language: LanguageMode) {
        context.languageDataStore.edit { preferences ->
            preferences[LanguagePreferenceKeys.LANGUAGE_KEY] = language.name
        }
    }
}

package com.apcs.worknestapp.data.local.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")

object ThemePreferenceKeys {
    val THEME_KEY = stringPreferencesKey("theme")
}

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT,
}

class ThemeDataStore @Inject constructor(
    private val context: Context,
) {
    val themeFlow: Flow<ThemeMode> = context.themeDataStore.data
        .map { preferences ->
            val value = preferences[ThemePreferenceKeys.THEME_KEY]
            ThemeMode.entries.find { it.name == value } ?: ThemeMode.SYSTEM
        }

    suspend fun saveTheme(theme: ThemeMode) {
        context.themeDataStore.edit { preferences ->
            preferences[ThemePreferenceKeys.THEME_KEY] = theme.name
        }
    }
}

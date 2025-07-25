package com.apcs.worknestapp.data.local.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeStore: ThemeDataStore,
) : ViewModel() {
    private val _theme = MutableStateFlow(ThemeMode.SYSTEM)
    val theme: StateFlow<ThemeMode> = _theme

    init {
        viewModelScope.launch {
            themeStore.themeFlow.collectLatest {
                _theme.value = it
            }
        }
    }

    fun saveTheme(theme: ThemeMode) {
        viewModelScope.launch {
            themeStore.saveTheme(theme)
        }
    }
}

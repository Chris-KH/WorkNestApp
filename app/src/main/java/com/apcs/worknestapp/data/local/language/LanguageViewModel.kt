package com.apcs.worknestapp.data.local.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val languageStore: LanguageDataStore,
) : ViewModel() {
    private val _language = MutableStateFlow(LanguageMode.EN_US)
    val language: StateFlow<LanguageMode> = _language

    init {
        viewModelScope.launch {
            languageStore.languageFlow.collectLatest {
                _language.value = it
            }
        }
    }

    fun saveLanguage(language: LanguageMode) {
        viewModelScope.launch {
            languageStore.saveLanguage(language)
        }
    }
}

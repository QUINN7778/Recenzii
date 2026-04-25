package com.sianov.stepan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sianov.stepan.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    val isDarkTheme: StateFlow<Boolean?> = repository.isDarkTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val fontSizeMultiplier: StateFlow<Float> = repository.fontSizeMultiplier
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

    val themeColorIndex: StateFlow<Int> = repository.themeColorIndex
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val seedColor: StateFlow<Long> = repository.seedColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0xFFB71C1C)

    fun setDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            repository.setDarkTheme(isDark)
        }
    }

    fun setFontSize(size: Float) {
        viewModelScope.launch {
            repository.setFontSize(size)
        }
    }

    fun setThemeColor(index: Int) {
        viewModelScope.launch {
            repository.setThemeColor(index)
        }
    }

    fun setSeedColor(color: Long) {
        viewModelScope.launch {
            repository.setSeedColor(color)
        }
    }
}

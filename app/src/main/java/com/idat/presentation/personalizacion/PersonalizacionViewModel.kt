package com.idat.presentation.personalizacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idat.data.local.preferences.UserPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonalizacionViewModel @Inject constructor(
    private val preferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    private val _viewMode = MutableStateFlow("grid")
    val viewMode: StateFlow<String> = _viewMode

    init {
        viewModelScope.launch {
            preferencesManager.isDarkTheme.collect { isDark ->
                _isDarkTheme.value = isDark
            }
        }
        viewModelScope.launch {
            preferencesManager.viewMode.collect { mode ->
                _viewMode.value = mode
            }
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            preferencesManager.setDarkTheme(!_isDarkTheme.value)
        }
    }

    fun setViewMode(mode: String) {
        viewModelScope.launch {
            preferencesManager.setViewMode(mode)
        }
    }
}

package com.idat.presentation.ayuda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idat.data.local.preferences.UserPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AyudaViewModel @Inject constructor(
    private val preferencesManager: UserPreferencesManager
) : ViewModel() {

    // Estado del tema oscuro
    val isDarkTheme = preferencesManager.isDarkTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // Estado para controlar qué FAQ está expandido (solo uno a la vez)
    private val _expandedFaqId = MutableStateFlow<Int?>(null)
    val expandedFaqId: StateFlow<Int?> = _expandedFaqId

    /**
     * Alterna el estado expandido/contraído de una pregunta frecuente
     * Si se hace clic en una pregunta que ya está expandida, se contrae
     * Si se hace clic en una nueva pregunta, la anterior se contrae y la nueva se expande
     */
    fun toggleFaq(faqId: Int) {
        viewModelScope.launch {
            _expandedFaqId.value = if (_expandedFaqId.value == faqId) {
                null // Si ya está expandido, lo contraemos
            } else {
                faqId // Si no está expandido o es otro, lo expandimos
            }
        }
    }

    /**
     * Cierra todas las FAQs expandidas
     */
    fun collapseAllFaqs() {
        viewModelScope.launch {
            _expandedFaqId.value = null
        }
    }
}

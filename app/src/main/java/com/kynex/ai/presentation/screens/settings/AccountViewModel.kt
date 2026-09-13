package com.kynex.ai.presentation.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynex.ai.di.AppGraph
import com.kynex.ai.domain.model.AiModel
import com.kynex.ai.domain.model.UserProfile
import kotlinx.coroutines.launch

data class AccountUiState(
    val profile: UserProfile? = null,
    val selectedModel: AiModel? = null,
    val isDarkTheme: Boolean = false,
    val error: String? = null
)

/** Serves both the Settings and Profile screens. */
class AccountViewModel(private val graph: AppGraph) : ViewModel() {

    var state by mutableStateOf(AccountUiState())
        private set

    init {
        viewModelScope.launch {
            graph.settingsStore.lastModelFlow.collect { id ->
                val m = com.kynex.ai.domain.model.ModelRegistry.byId(id)
                if (m != null) state = state.copy(selectedModel = m)
            }
        }
        viewModelScope.launch {
            graph.settingsStore.themeFlow.collect { theme ->
                state = state.copy(isDarkTheme = theme == com.kynex.ai.data.prefs.SettingsDataStore.THEME_DARK)
            }
        }
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            val uid = graph.authRepository.currentUserId() ?: return@launch
            runCatching { graph.chatRepository(uid).loadProfile() }
                .onSuccess { state = state.copy(profile = it, error = null) }
                .onFailure { state = state.copy(error = it.message) }
        }
    }

    fun updateName(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val uid = graph.authRepository.currentUserId() ?: return@launch
            runCatching { graph.chatRepository(uid).updateDisplayName(name.trim()) }
                .onSuccess { loadProfile() }
                .onFailure { state = state.copy(error = it.message) }
        }
    }

    fun selectModel(model: AiModel) {
        state = state.copy(selectedModel = model)
        viewModelScope.launch { graph.settingsStore.setLastModel(model.id) }
    }

    fun toggleTheme() {
        val next = !state.isDarkTheme
        state = state.copy(isDarkTheme = next)
        viewModelScope.launch {
            graph.settingsStore.setTheme(
                if (next) com.kynex.ai.data.prefs.SettingsDataStore.THEME_DARK
                else com.kynex.ai.data.prefs.SettingsDataStore.THEME_LIGHT
            )
        }
    }

    fun logout() {
        graph.authRepository.signOut()
    }
}

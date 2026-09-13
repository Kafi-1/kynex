package com.kynex.ai.presentation.screens.imagegen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynex.ai.di.AppGraph
import kotlinx.coroutines.launch

data class ImageGenUiState(
    val prompt: String = "",
    val isLoading: Boolean = false,
    val imageBytes: ByteArray? = null,
    val revisedPrompt: String? = null,
    val error: String? = null
) {
    val canGenerate: Boolean get() = prompt.isNotBlank() && !isLoading
}

class ImageGenViewModel(private val graph: AppGraph) : ViewModel() {

    var state by mutableStateOf(ImageGenUiState())
        private set

    fun onPromptChange(v: String) {
        state = state.copy(prompt = v, error = null)
    }

    /** The user's full natural-language request is sent as the prompt, unchanged. */
    fun generate() {
        val prompt = state.prompt.trim()
        if (prompt.isEmpty() || state.isLoading) return
        state = state.copy(isLoading = true, error = null, imageBytes = null, revisedPrompt = null)

        viewModelScope.launch {
            val result = graph.imageService.generate(prompt)
            result.onSuccess { bytes ->
                state = state.copy(isLoading = false, imageBytes = bytes)
            }.onFailure { e ->
                state = state.copy(isLoading = false, error = e.message ?: "Generation failed. Please try again.")
            }
        }
    }

    fun retry() = generate()

    fun reset() {
        state = ImageGenUiState()
    }

    fun dismissError() {
        state = state.copy(error = null)
    }
}

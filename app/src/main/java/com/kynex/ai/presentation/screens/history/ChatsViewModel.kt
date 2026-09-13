package com.kynex.ai.presentation.screens.history

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynex.ai.di.AppGraph
import com.kynex.ai.domain.model.Chat
import kotlinx.coroutines.launch

data class ChatsUiState(
    val chats: List<Chat> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
) {
    val visible: List<Chat>
        get() = if (searchQuery.isBlank()) chats
        else chats.filter { it.title.contains(searchQuery, ignoreCase = true) }
}

/** Shared by the History and Saved screens. */
open class ChatsViewModel(
    private val graph: AppGraph,
    private val savedOnly: Boolean = false
) : ViewModel() {

    var state by mutableStateOf(ChatsUiState())
        private set

    init {
        viewModelScope.launch {
            val uid = graph.authRepository.currentUserId() ?: return@launch
            runCatching {
                graph.chatRepository(uid).observeChats().collect { all ->
                    state = state.copy(chats = if (savedOnly) all.filter { it.isSaved } else all)
                }
            }.onFailure { state = state.copy(error = it.message) }
        }
    }

    fun onSearchChange(v: String) {
        state = state.copy(searchQuery = v)
    }

    fun rename(chatId: String, title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val uid = graph.authRepository.currentUserId() ?: return@launch
            runCatching { graph.chatRepository(uid).renameChat(chatId, title.trim()) }
                .onFailure { state = state.copy(error = it.message) }
        }
    }

    fun delete(chatId: String) {
        viewModelScope.launch {
            val uid = graph.authRepository.currentUserId() ?: return@launch
            runCatching { graph.chatRepository(uid).deleteChat(chatId) }
                .onFailure { state = state.copy(error = it.message) }
        }
    }

    fun toggleSaved(chat: Chat) {
        viewModelScope.launch {
            val uid = graph.authRepository.currentUserId() ?: return@launch
            runCatching { graph.chatRepository(uid).setSaved(chat.id, !chat.isSaved) }
                .onFailure { state = state.copy(error = it.message) }
        }
    }
}

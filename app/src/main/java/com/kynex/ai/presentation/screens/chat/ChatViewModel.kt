package com.kynex.ai.presentation.screens.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynex.ai.data.firestore.ChatRepository
import com.kynex.ai.di.AppGraph
import com.kynex.ai.domain.model.AiModel
import com.kynex.ai.domain.model.Chat
import com.kynex.ai.domain.model.ChatMessage
import com.kynex.ai.domain.model.ModelRegistry
import com.kynex.ai.domain.model.Role
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class ChatUiState(
    val chats: List<Chat> = emptyList(),
    val remoteMessages: List<ChatMessage> = emptyList(),
    val pending: List<ChatMessage> = emptyList(),
    val currentChatId: String? = null,
    val title: String = "",
    val selectedModel: AiModel = com.kynex.ai.data.network.AiConfig.DEFAULT_MODEL,
    val input: String = "",
    val isStreaming: Boolean = false,
    val streamingText: String = "",
    val error: String? = null
) {
    /** Firestore messages plus optimistic local writes not yet confirmed. */
    val messages: List<ChatMessage>
        get() {
            val remoteIds = remoteMessages.mapTo(mutableSetOf()) { it.id }
            return remoteMessages + pending.filter { it.id !in remoteIds }
        }
}

class ChatViewModel(private val graph: AppGraph) : ViewModel() {

    var state by mutableStateOf(ChatUiState())
        private set

    private var uid: String? = null
    private var repo: ChatRepository? = null
    private var chatsJob: Job? = null
    private var messagesJob: Job? = null
    private var streamJob: Job? = null
    private var messageLimit: Long = 60
    private var modelLoadedFromPrefs = false

    init {
        viewModelScope.launch {
            graph.authRepository.authStateFlow().collect { newUid ->
                if (newUid != uid) {
                    uid = newUid
                    repo = newUid?.let { graph.chatRepository(it) }
                    resetChatState()
                    if (newUid != null) observeChats()
                }
            }
        }
        viewModelScope.launch {
            graph.settingsStore.lastModelFlow.collect { id ->
                val m = ModelRegistry.byId(id)
                if (m != null && !modelLoadedFromPrefs && state.currentChatId == null) {
                    modelLoadedFromPrefs = true
                    state = state.copy(selectedModel = m)
                }
            }
        }
    }

    private fun resetChatState() {
        streamJob?.cancel()
        messagesJob?.cancel()
        messageLimit = 60
        state = ChatUiState(selectedModel = state.selectedModel)
    }

    private fun observeChats() {
        chatsJob?.cancel()
        chatsJob = viewModelScope.launch {
            runCatching {
                repo?.observeChats()?.collect { chats ->
                    state = state.copy(chats = chats)
                }
            }.onFailure { e ->
                state = state.copy(error = friendlyError(e))
            }
        }
    }

    fun onInputChange(v: String) {
        state = state.copy(input = v)
    }

    fun selectModel(model: AiModel) {
        state = state.copy(selectedModel = model)
        viewModelScope.launch { graph.settingsStore.setLastModel(model.id) }
        val chatId = state.currentChatId ?: return
        viewModelScope.launch {
            runCatching { repo?.updateChatModel(chatId, model.id) }
        }
    }

    fun newChat() {
        if (state.currentChatId == null && state.messages.isEmpty()) return
        streamJob?.cancel()
        messagesJob?.cancel()
        state = state.copy(
            currentChatId = null,
            title = "",
            remoteMessages = emptyList(),
            pending = emptyList(),
            input = "",
            isStreaming = false,
            streamingText = "",
            error = null
        )
    }

    fun openChat(chatId: String) {
        if (chatId.isBlank() || chatId == state.currentChatId) return
        streamJob?.cancel()
        messagesJob?.cancel()
        messageLimit = 60
        val chat = state.chats.firstOrNull { it.id == chatId }
        state = state.copy(
            currentChatId = chatId,
            title = chat?.title ?: "",
            selectedModel = chat?.model ?: state.selectedModel,
            remoteMessages = emptyList(),
            pending = emptyList(),
            input = "",
            isStreaming = false,
            streamingText = "",
            error = null
        )
        observeMessages(chatId)
    }

    private fun observeMessages(chatId: String) {
        messagesJob = viewModelScope.launch {
            runCatching {
                repo?.observeMessages(chatId, messageLimit)?.collect { list ->
                    state = state.copy(remoteMessages = list)
                }
            }.onFailure { e ->
                state = state.copy(error = friendlyError(e))
            }
        }
    }

    fun loadEarlierMessages() {
        val chatId = state.currentChatId ?: return
        if (messageLimit >= 500) return
        messageLimit += 60
        observeMessages(chatId)
    }

    fun sendMessage() {
        val text = state.input.trim()
        if (text.isEmpty() || state.isStreaming) return
        val model = state.selectedModel
        val repository = repo ?: return

        viewModelScope.launch {
            try {
                var chatId = state.currentChatId
                if (chatId == null) {
                    val title = text.take(40)
                    val chat = repository.createChat(title, model.id)
                    chatId = chat.id
                    state = state.copy(currentChatId = chatId, title = title)
                    observeMessages(chatId)
                }

                val userMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    role = Role.USER,
                    content = text,
                    createdAt = System.currentTimeMillis()
                )
                state = state.copy(
                    pending = state.pending + userMsg,
                    input = ""
                )
                repository.appendMessage(chatId, userMsg)
                runStream(chatId, model)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                state = state.copy(isStreaming = false, error = friendlyError(e))
            }
        }
    }

    fun regenerate() {
        val chatId = state.currentChatId ?: return
        val last = state.messages.lastOrNull() ?: return
        if (state.isStreaming || last.role != Role.ASSISTANT) return
        viewModelScope.launch {
            state = state.copy(remoteMessages = state.remoteMessages.filterNot { it.id == last.id })
            runCatching { repo?.deleteMessage(chatId, last.id) }
            runStream(chatId, state.selectedModel)
        }
    }

    fun retryLast() {
        val chatId = state.currentChatId ?: return
        val last = state.messages.lastOrNull() ?: return
        if (state.isStreaming) return
        viewModelScope.launch {
            if (last.isError) {
                state = state.copy(remoteMessages = state.remoteMessages.filterNot { it.id == last.id })
                runCatching { repo?.deleteMessage(chatId, last.id) }
            }
            runStream(chatId, state.selectedModel)
        }
    }

    fun stopGeneration() {
        val text = state.streamingText
        val chatId = state.currentChatId
        val model = state.selectedModel
        streamJob?.cancel()
        streamJob = null
        state = state.copy(isStreaming = false, streamingText = "")
        if (text.isBlank() || chatId == null) return
        viewModelScope.launch {
            withContext(NonCancellable) { completeStream(chatId, model, text) }
        }
    }

    private fun runStream(chatId: String, model: AiModel) {
        streamJob?.cancel()
        streamJob = viewModelScope.launch {
            state = state.copy(isStreaming = true, streamingText = "", error = null)
            val all = state.messages.filter { !it.isError }
            val lastUserIdx = all.indexOfLast { it.role == Role.USER }
            val history = if (lastUserIdx >= 0) all.take(lastUserIdx + 1) else all

            var received = false
            try {
                graph.authService.streamChat(model.id, history).collect { delta ->
                    received = true
                    state = state.copy(streamingText = state.streamingText + delta)
                }
                completeStream(chatId, model, state.streamingText)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (!received) {
                    // Non-streaming fallback
                    try {
                        val text = graph.authService.chatOnce(model.id, history)
                        state = state.copy(streamingText = text)
                        completeStream(chatId, model, text)
                    } catch (e2: Exception) {
                        failStream(chatId, model, e2)
                    }
                } else {
                    // Keep partial answer
                    completeStream(chatId, model, state.streamingText)
                }
            }
        }
    }

    private suspend fun completeStream(chatId: String, model: AiModel, text: String) {
        val repository = repo ?: return
        if (text.isBlank()) {
            state = state.copy(isStreaming = false, streamingText = "")
            return
        }
        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = Role.ASSISTANT,
            content = text,
            modelId = model.id,
            createdAt = System.currentTimeMillis(),
            isError = false
        )
        runCatching {
            repository.appendMessage(chatId, msg)
            repository.touchChat(
                chatId = chatId,
                modelId = model.id,
                lastMessage = text,
                countDelta = 2
            )
        }
        state = state.copy(isStreaming = false, streamingText = "")
    }

    private suspend fun failStream(chatId: String, model: AiModel, e: Exception) {
        val repository = repo ?: return
        val friendly = friendlyError(e)
        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = Role.ASSISTANT,
            content = friendly,
            modelId = model.id,
            createdAt = System.currentTimeMillis(),
            isError = true
        )
        runCatching { repository.appendMessage(chatId, msg) }
        runCatching { repository.touchChat(chatId, lastMessage = "Request failed", countDelta = 2) }
        state = state.copy(isStreaming = false, streamingText = "", error = friendly)
    }

    fun dismissError() {
        state = state.copy(error = null)
    }

    // ---------- Sidebar actions ----------

    fun renameChat(chatId: String, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            runCatching { repo?.renameChat(chatId, newTitle.trim()) }
                .onFailure { state = state.copy(error = friendlyError(it)) }
        }
        if (chatId == state.currentChatId) {
            state = state.copy(title = newTitle.trim())
        }
    }

    fun toggleSaved(chat: Chat) {
        viewModelScope.launch {
            runCatching { repo?.setSaved(chat.id, !chat.isSaved) }
                .onFailure { state = state.copy(error = friendlyError(it)) }
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            runCatching { repo?.deleteChat(chatId) }
                .onFailure { state = state.copy(error = friendlyError(it)) }
            if (chatId == state.currentChatId) newChat()
        }
    }

    private fun friendlyError(e: Throwable): String {
        val msg = e.message ?: return "Something went wrong. Please try again."
        return when {
            "Unable to resolve" in msg || "network" in msg.lowercase() ->
                "No internet connection. Please check and try again."
            "HTTP 401" in msg || "401" in msg -> "Authentication failed — check the AgentRouter API key."
            "HTTP 429" in msg || "429" in msg -> "Rate limit reached. Please wait a moment and retry."
            "HTTP 5" in msg -> "AgentRouter server error. Please retry."
            "timeout" in msg.lowercase() -> "The request timed out. Please retry."
            else -> msg.take(200)
        }
    }
}

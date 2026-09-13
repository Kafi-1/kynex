package com.kynex.ai.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kynex.ai.di.AppGraph
import com.kynex.ai.presentation.screens.auth.AuthViewModel
import com.kynex.ai.presentation.screens.chat.ChatViewModel
import com.kynex.ai.presentation.screens.history.ChatsViewModel
import com.kynex.ai.presentation.screens.imagegen.ImageGenViewModel
import com.kynex.ai.presentation.screens.saved.SavedChatsViewModel

class VMFactory(private val graph: AppGraph) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(graph)
        modelClass.isAssignableFrom(ChatViewModel::class.java) -> ChatViewModel(graph)
        modelClass.isAssignableFrom(ImageGenViewModel::class.java) -> ImageGenViewModel(graph)
        // SavedChatsViewModel must be checked before its parent ChatsViewModel
        modelClass.isAssignableFrom(SavedChatsViewModel::class.java) -> SavedChatsViewModel(graph)
        modelClass.isAssignableFrom(ChatsViewModel::class.java) -> ChatsViewModel(graph)
        else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    } as T
}

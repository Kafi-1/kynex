package com.kynex.ai.presentation.screens.saved

import com.kynex.ai.di.AppGraph
import com.kynex.ai.presentation.screens.history.ChatsViewModel

/** Saved chats variant of the shared chats list ViewModel. */
class SavedChatsViewModel(graph: AppGraph) : ChatsViewModel(graph, savedOnly = true)

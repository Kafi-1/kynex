package com.kynex.ai.presentation.screens.saved

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kynex.ai.di.AppGraph
import com.kynex.ai.presentation.VMFactory
import com.kynex.ai.presentation.screens.history.ChatsListScreen

/** Saved/bookmarked conversations, backed by Firestore isSaved flag. */
@Composable
fun SavedScreen(
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit
) {
    val vm: SavedChatsViewModel = viewModel(key = "saved_chats", factory = VMFactory(AppGraph))
    ChatsListScreen(
        title = "Saved chats",
        vm = vm,
        onBack = onBack,
        onOpenChat = onOpenChat
    )
}

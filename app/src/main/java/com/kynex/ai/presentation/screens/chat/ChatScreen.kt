package com.kynex.ai.presentation.screens.chat

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kynex.ai.core.theme.Kynex
import com.kynex.ai.core.theme.SerifDisplay
import com.kynex.ai.data.prefs.SettingsDataStore
import com.kynex.ai.di.AppGraph
import com.kynex.ai.presentation.VMFactory
import com.kynex.ai.presentation.components.ChatInputBar
import com.kynex.ai.presentation.components.MarkdownText
import com.kynex.ai.presentation.components.MessageRow
import com.kynex.ai.presentation.components.ModelSelector
import com.kynex.ai.presentation.components.SparkleLogo
import com.kynex.ai.presentation.components.TypingDots
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.Calendar

private fun greeting(): String {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        h < 5 -> "Up late?"
        h < 12 -> "Good morning"
        h < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}

private val SUGGESTIONS = listOf("Write a poem", "Explain a concept", "Help me code", "Brainstorm ideas")

@Composable
fun ChatScreen(
    openChatId: String,
    onNavigateToHistory: () -> Unit,
    onNavigateToSaved: () -> Unit,
    onNavigateToImageGen: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onOpenChat: (String) -> Unit
) {
    val activity = LocalContext.current as? ComponentActivity
    val vm: ChatViewModel = if (activity != null) {
        viewModel(viewModelStoreOwner = activity, factory = VMFactory(AppGraph))
    } else {
        viewModel(factory = VMFactory(AppGraph))
    }
    val state = vm.state
    val colors = Kynex.colors
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    val theme by AppGraph.settingsStore.themeFlow
        .collectAsStateWithLifecycle(initialValue = SettingsDataStore.THEME_LIGHT)
    val isDark = theme == SettingsDataStore.THEME_DARK

    var searchQuery by remember { mutableStateOf("") }
    var sidebarOpen by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(openChatId) {
        if (openChatId.isNotBlank()) vm.openChat(openChatId)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        val isWide = maxWidth >= 840.dp
        val showPermanent = isWide && sidebarOpen
        val showDrawer = !isWide && sidebarOpen

        // Edge-swipe: right-swipe from left edge opens the sidebar,
        // left-swipe anywhere closes it (matches demo.html touch behavior).
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isWide, sidebarOpen) {
                    detectHorizontalDragGestures(
                        onDragEnd = {}
                    ) { change, dragAmount ->
                        if (isWide) return@detectHorizontalDragGestures
                        if (sidebarOpen) {
                            if (dragAmount < -55) sidebarOpen = false
                        } else {
                            val fromEdge = change.position.x < 60f
                            if (dragAmount > 55 && fromEdge) sidebarOpen = true
                        }
                    }
                }
        ) {

        Row(modifier = Modifier.fillMaxSize()) {
            if (showPermanent) {
                SidebarContent(
                    userName = AppGraph.authRepository.currentUserName(),
                    chats = state.chats,
                    currentChatId = state.currentChatId,
                    searchQuery = searchQuery,
                    isDarkTheme = isDark,
                    onSearchQueryChange = { searchQuery = it },
                    onNewChat = { vm.newChat(); if (!isWide) sidebarOpen = false },
                    onOpenChat = { id ->
                        onOpenChat(id)
                        if (!isWide) sidebarOpen = false
                    },
                    onRename = vm::renameChat,
                    onDelete = vm::deleteChat,
                    onToggleSave = vm::toggleSaved,
                    onToggleTheme = {
                        scope.launch {
                            AppGraph.settingsStore.setTheme(
                                if (isDark) SettingsDataStore.THEME_LIGHT else SettingsDataStore.THEME_DARK
                            )
                        }
                    },
                    onCollapse = { sidebarOpen = false },
                    onOpenHistory = onNavigateToHistory,
                    onOpenSaved = onNavigateToSaved,
                    onOpenImageGen = onNavigateToImageGen,
                    onOpenSettings = onNavigateToSettings,
                    onOpenProfile = onNavigateToProfile
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!showPermanent) {
                        IconButton(onClick = { sidebarOpen = true }) {
                            Icon(Icons.Rounded.Menu, "Open sidebar", tint = colors.textSecondary)
                        }
                    }
                    ModelSelector(
                        selected = state.selectedModel,
                        models = com.kynex.ai.data.network.AiConfig.MODELS,
                        onSelect = vm::selectModel
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { vm.newChat() }) {
                        Icon(Icons.Rounded.Edit, "New chat", tint = colors.textSecondary)
                    }
                }

                // Error banner
                if (state.error != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.bgHover)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.error ?: "",
                            color = colors.textSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = vm::dismissError, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Rounded.Close, "Dismiss", tint = colors.textTertiary, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                ChatArea(
                    state = state,
                    onCopy = { clipboard.setText(AnnotatedString(it)) },
                    onRegenerate = vm::regenerate,
                    onRetry = vm::retryLast,
                    onSuggestion = { text ->
                        vm.onInputChange(text)
                        vm.sendMessage()
                    },
                    onLoadEarlier = vm::loadEarlierMessages,
                    modifier = Modifier.weight(1f)
                )

                ChatInputBar(
                    value = state.input,
                    onValueChange = vm::onInputChange,
                    onSend = vm::sendMessage,
                    onStop = vm::stopGeneration,
                    isStreaming = state.isStreaming
                )
                Spacer(Modifier.navigationBarsPadding())
            }
        }

        // Mobile drawer
        if (showDrawer) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f))
                    .clickable { sidebarOpen = false }
            )
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.align(Alignment.CenterStart)) {
                    SidebarContent(
                        userName = AppGraph.authRepository.currentUserName(),
                        chats = state.chats,
                        currentChatId = state.currentChatId,
                        searchQuery = searchQuery,
                        isDarkTheme = isDark,
                        onSearchQueryChange = { searchQuery = it },
                        onNewChat = { vm.newChat(); sidebarOpen = false },
                        onOpenChat = { id ->
                            onOpenChat(id)
                            sidebarOpen = false
                        },
                        onRename = vm::renameChat,
                        onDelete = vm::deleteChat,
                        onToggleSave = vm::toggleSaved,
                        onToggleTheme = {
                            scope.launch {
                                AppGraph.settingsStore.setTheme(
                                    if (isDark) SettingsDataStore.THEME_LIGHT else SettingsDataStore.THEME_DARK
                                )
                            }
                        },
                        onCollapse = { sidebarOpen = false },
                        onOpenHistory = onNavigateToHistory,
                        onOpenSaved = onNavigateToSaved,
                        onOpenImageGen = onNavigateToImageGen,
                        onOpenSettings = onNavigateToSettings,
                        onOpenProfile = onNavigateToProfile
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun ChatArea(
    state: ChatUiState,
    onCopy: (String) -> Unit,
    onRegenerate: () -> Unit,
    onRetry: () -> Unit,
    onSuggestion: (String) -> Unit,
    onLoadEarlier: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = Kynex.colors
    val listState = rememberLazyListState()
    val messages = state.messages
    val lastAssistantId = messages.lastOrNull { it.role == com.kynex.ai.domain.model.Role.ASSISTANT }?.id

    LaunchedEffect(messages.size, state.streamingText) {
        val count = listState.layoutInfo.totalItemsCount
        if (count > 0) listState.animateScrollToItem(count - 1)
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex == 0 }
            .distinctUntilChanged()
            .collect { atTop -> if (atTop) onLoadEarlier() }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (messages.isEmpty() && !state.isStreaming) {
            item {
                Column(
                    modifier = Modifier
                        .fillParentMaxHeight()
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    SparkleLogo(size = 44.dp, tint = colors.accent)
                    Spacer(Modifier.height(22.dp))
                    Text(
                        text = greeting(),
                        fontFamily = SerifDisplay,
                        fontSize = 32.sp,
                        color = colors.text
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "How can I help you today?",
                        fontSize = 15.sp,
                        color = colors.textTertiary
                    )
                    Spacer(Modifier.height(26.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        SUGGESTIONS.chunked(2).forEach { rowItems ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                rowItems.forEach { chip ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(colors.bgInput)
                                            .clickable { onSuggestion(chip) }
                                            .padding(horizontal = 16.dp, vertical = 9.dp)
                                    ) {
                                        Text(
                                            chip,
                                            fontSize = 13.5.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }
        }

        items(messages, key = { it.id }) { message ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 768.dp)
                    .padding(horizontal = 24.dp)
            ) {
                MessageRow(
                    message = message,
                    isLastAssistant = message.id == lastAssistantId && !state.isStreaming,
                    onCopy = onCopy,
                    onRegenerate = onRegenerate,
                    onRetry = onRetry
                )
            }
        }

        if (state.isStreaming) {
            item(key = "streaming") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 768.dp)
                        .padding(horizontal = 24.dp)
                ) {
                    if (state.streamingText.isBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SparkleLogo(size = 22.dp, tint = colors.accent)
                            Spacer(Modifier.width(10.dp))
                            TypingDots()
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SparkleLogo(size = 22.dp, tint = colors.accent)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Kynex AI",
                                fontSize = 13.sp,
                                color = colors.textTertiary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        MarkdownText(markdown = state.streamingText)
                    }
                }
            }
        }

        item(key = "bottom_space") { Spacer(Modifier.height(28.dp)) }
    }
}

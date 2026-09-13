package com.kynex.ai.presentation.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DriveFileRenameOutline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kynex.ai.core.theme.Kynex
import com.kynex.ai.di.AppGraph
import com.kynex.ai.domain.model.Chat
import com.kynex.ai.presentation.VMFactory
import com.kynex.ai.presentation.screens.chat.groupChatsByDate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Full-screen chat history with rename/delete/save actions. */
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit
) {
    val vm: ChatsViewModel = viewModel(key = "history_chats", factory = VMFactory(AppGraph))
    ChatsListScreen(
        title = "Chat history",
        vm = vm,
        onBack = onBack,
        onOpenChat = onOpenChat
    )
}

@Composable
internal fun ChatsListScreen(
    title: String,
    vm: ChatsViewModel,
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit
) {
    val state = vm.state
    val colors = Kynex.colors
    var renameTarget by remember { mutableStateOf<Chat?>(null) }
    var deleteTarget by remember { mutableStateOf<Chat?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = colors.textSecondary)
            }
            Text(
                title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text
            )
        }

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = vm::onSearchChange,
            singleLine = true,
            placeholder = { Text("Search chats...", fontSize = 14.sp, color = colors.textTertiary) },
            leadingIcon = { Icon(Icons.Rounded.Search, null, tint = colors.textTertiary, modifier = Modifier.size(18.dp)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (state.error != null) {
            Text(
                state.error ?: "",
                color = colors.textSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        val grouped = groupChatsByDate(state.visible)

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            grouped.forEach { (label, groupChats) ->
                item(key = "label_$label") {
                    Text(
                        text = label.uppercase(Locale.ROOT),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textTertiary,
                        modifier = Modifier.padding(start = 22.dp, top = 14.dp, bottom = 6.dp)
                    )
                }
                items(groupChats, key = { it.id }) { chat ->
                    ChatCard(
                        chat = chat,
                        onClick = { onOpenChat(chat.id) },
                        onRename = { renameTarget = chat },
                        onDelete = { deleteTarget = chat },
                        onToggleSave = { vm.toggleSaved(chat) }
                    )
                }
            }
            if (grouped.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            if (state.searchQuery.isBlank())
                                "No conversations yet"
                            else "No chats match your search",
                            fontSize = 14.sp,
                            color = colors.textTertiary
                        )
                    }
                }
            }
        }
    }

    if (renameTarget != null) {
        val chat = renameTarget!!
        var title2 by remember(chat.id) { mutableStateOf(chat.title) }
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Rename chat") },
            text = {
                OutlinedTextField(
                    value = title2,
                    onValueChange = { title2 = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.rename(chat.id, title2)
                        renameTarget = null
                    },
                    enabled = title2.isNotBlank()
                ) { Text("Save", color = colors.accent) }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("Cancel") }
            },
            containerColor = colors.bgInput
        )
    }

    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete chat?") },
            text = { Text("\"${deleteTarget!!.title}\" will be permanently deleted. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.delete(deleteTarget!!.id)
                    deleteTarget = null
                }) { Text("Delete", color = colors.accent) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            },
            containerColor = colors.bgInput
        )
    }
}

@Composable
private fun ChatCard(
    chat: Chat,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onToggleSave: () -> Unit
) {
    val colors = Kynex.colors
    val dateFormat = remember { SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.bgInput)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = chat.title.ifBlank { "Untitled" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (chat.isSaved) {
                    Spacer(Modifier.size(6.dp))
                    Icon(Icons.Rounded.Bookmark, null, tint = colors.accent, modifier = Modifier.size(13.dp))
                }
            }
            if (chat.lastMessage.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = chat.lastMessage,
                    fontSize = 13.sp,
                    color = colors.textTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = dateFormat.format(Date(chat.updatedAt)),
                    fontSize = 11.5.sp,
                    color = colors.textTertiary
                )
                Text(
                    text = "·",
                    fontSize = 11.5.sp,
                    color = colors.textTertiary
                )
                Text(
                    text = chat.model?.displayName ?: chat.modelId,
                    fontSize = 11.5.sp,
                    color = colors.textTertiary
                )
            }
        }
        IconButton(onClick = onToggleSave) {
            Icon(
                if (chat.isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                contentDescription = if (chat.isSaved) "Unsave" else "Save",
                tint = if (chat.isSaved) colors.accent else colors.textTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
        IconButton(onClick = onRename) {
            Icon(Icons.Rounded.DriveFileRenameOutline, "Rename", tint = colors.textTertiary, modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Rounded.Delete, "Delete", tint = colors.textTertiary, modifier = Modifier.size(18.dp))
        }
    }
}

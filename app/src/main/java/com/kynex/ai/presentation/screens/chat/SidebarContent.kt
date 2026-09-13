package com.kynex.ai.presentation.screens.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.MenuOpen
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kynex.ai.core.theme.Kynex
import com.kynex.ai.domain.model.Chat
import java.util.Calendar
import java.util.Locale

/** Groups chats into Today / Yesterday / Previous 7 days / Older (demo.html style). */
fun groupChatsByDate(chats: List<Chat>): List<Pair<String, List<Chat>>> {
    val now = Calendar.getInstance()
    val startOfToday = (now.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val startOfYesterday = startOfToday - 86_400_000L
    val startOf7Days = startOfToday - 7 * 86_400_000L

    val groups = linkedMapOf(
        "Today" to mutableListOf<Chat>(),
        "Yesterday" to mutableListOf<Chat>(),
        "Previous 7 days" to mutableListOf<Chat>(),
        "Older" to mutableListOf<Chat>()
    )
    chats.sortedByDescending { it.updatedAt }.forEach { chat ->
        when {
            chat.updatedAt >= startOfToday -> groups.getValue("Today").add(chat)
            chat.updatedAt >= startOfYesterday -> groups.getValue("Yesterday").add(chat)
            chat.updatedAt >= startOf7Days -> groups.getValue("Previous 7 days").add(chat)
            else -> groups.getValue("Older").add(chat)
        }
    }
    return groups.filter { it.value.isNotEmpty() }.toList()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SidebarContent(
    userName: String,
    chats: List<Chat>,
    currentChatId: String?,
    searchQuery: String,
    isDarkTheme: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onNewChat: () -> Unit,
    onOpenChat: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    onToggleSave: (Chat) -> Unit,
    onToggleTheme: () -> Unit,
    onCollapse: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSaved: () -> Unit,
    onOpenImageGen: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val colors = Kynex.colors
    var actionChat by remember { mutableStateOf<Chat?>(null) }
    var showRename by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    val filtered = remember(chats, searchQuery) {
        if (searchQuery.isBlank()) chats
        else chats.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }
    val grouped = groupChatsByDate(filtered)

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(colors.bgSidebar)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCollapse) {
                Icon(Icons.Rounded.MenuOpen, "Close sidebar", tint = colors.textSecondary)
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onToggleTheme) {
                Icon(
                    if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                    contentDescription = "Toggle theme",
                    tint = colors.textSecondary
                )
            }
        }

        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                .clickable(onClick = onNewChat)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Icon(Icons.Rounded.Edit, null, tint = colors.text, modifier = Modifier.size(17.dp))
            Text("New chat", color = colors.text, fontSize = 14.5.sp)
        }

        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.bgHover)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Search,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                singleLine = true,
                textStyle = TextStyle(color = colors.text, fontSize = 14.sp),
                cursorBrush = SolidColor(colors.accent),
                decorationBox = { inner ->
                    Box(Modifier.fillMaxWidth()) {
                        if (searchQuery.isEmpty()) {
                            Text("Search chats...", fontSize = 14.sp, color = colors.textTertiary)
                        }
                        inner()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
            grouped.forEach { (label, groupChats) ->
                item(key = "label_$label") {
                    Text(
                        text = label.uppercase(Locale.ROOT),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textTertiary,
                        letterSpacing = 0.6.sp,
                        modifier = Modifier.padding(start = 8.dp, top = 14.dp, bottom = 6.dp)
                    )
                }
                items(groupChats, key = { it.id }) { chat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (chat.id == currentChatId) colors.bgHover else Color.Transparent)
                            .combinedClickable(
                                onClick = { onOpenChat(chat.id) },
                                onLongClick = {
                                    actionChat = chat
                                    showRename = true
                                }
                            )
                            .padding(horizontal = 10.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = chat.title.ifBlank { "Untitled" },
                            fontSize = 14.sp,
                            color = if (chat.id == currentChatId) colors.text else colors.textSecondary,
                            fontWeight = if (chat.id == currentChatId) FontWeight.Medium else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.weight(1f))
                        if (chat.isSaved) {
                            Icon(
                                Icons.Rounded.Bookmark,
                                contentDescription = "Saved",
                                tint = colors.accent,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
            if (grouped.isEmpty()) {
                item {
                    Text(
                        if (searchQuery.isBlank()) "No conversations yet"
                        else "No chats match your search",
                        fontSize = 13.sp,
                        color = colors.textTertiary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.bgHover)
                .clickable(onClick = onOpenImageGen)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Icon(
                Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(17.dp)
            )
            Text("Image Generate", color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onOpenHistory) {
                Icon(Icons.Rounded.History, "Chat history", tint = colors.textSecondary, modifier = Modifier.size(19.dp))
            }
            IconButton(onClick = onOpenSaved) {
                Icon(Icons.Rounded.BookmarkBorder, "Saved chats", tint = colors.textSecondary, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Rounded.Settings, "Settings", tint = colors.textSecondary, modifier = Modifier.size(19.dp))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onOpenProfile)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(colors.accent, colors.accentHover))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.trim().firstOrNull()?.uppercase() ?: "U",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    userName.ifBlank { "User" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("Free plan", fontSize = 11.5.sp, color = colors.textTertiary)
            }
        }
    }

    if (showRename && actionChat != null) {
        RenameDialog(
            initialTitle = actionChat!!.title,
            isSaved = actionChat!!.isSaved,
            onDismiss = { showRename = false },
            onConfirm = { newTitle ->
                onRename(actionChat!!.id, newTitle)
                showRename = false
            },
            onDelete = {
                showRename = false
                showDelete = true
            },
            onToggleSave = {
                onToggleSave(actionChat!!)
                showRename = false
            }
        )
    }

    if (showDelete && actionChat != null) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete chat?") },
            text = { Text("\"${actionChat!!.title}\" will be permanently deleted. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(actionChat!!.id)
                    showDelete = false
                }) { Text("Delete", color = colors.accent) }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Cancel") }
            },
            containerColor = colors.bgInput
        )
    }
}

@Composable
private fun RenameDialog(
    initialTitle: String,
    isSaved: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onDelete: () -> Unit,
    onToggleSave: () -> Unit
) {
    val colors = Kynex.colors
    var title by remember { mutableStateOf(initialTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename chat") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                    TextButton(onClick = onToggleSave) {
                        Icon(
                            if (isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                            null,
                            tint = colors.accent,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(if (isSaved) "Unsave" else "Save", color = colors.accent)
                    }
                    TextButton(onClick = onDelete) {
                        Icon(Icons.Rounded.Delete, null, tint = colors.textTertiary, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Delete", color = colors.textSecondary)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(title) }, enabled = title.isNotBlank()) {
                Text("Save", color = colors.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = colors.bgInput
    )
}

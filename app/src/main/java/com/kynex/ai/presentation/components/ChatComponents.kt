package com.kynex.ai.presentation.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kynex.ai.core.theme.Kynex
import com.kynex.ai.domain.model.AiModel
import com.kynex.ai.domain.model.ChatMessage
import com.kynex.ai.domain.model.Role

/** Three bouncing dots, matching demo.html typing indicator. */
@Composable
fun TypingDots() {
    val transition = rememberInfiniteTransition(label = "typing")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600)),
        label = "alpha"
    )
    val colors = Kynex.colors
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(8.dp)) {
        repeat(3) { index ->
            val delayFactor = (index + 1) * 0.2f
            Box(
                Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(colors.textTertiary.copy(alpha = alpha * delayFactor.coerceAtMost(1f)))
            )
        }
    }
}

@Composable
fun MessageRow(
    message: ChatMessage,
    isLastAssistant: Boolean,
    onCopy: (String) -> Unit,
    onRegenerate: () -> Unit,
    onRetry: () -> Unit
) {
    val colors = Kynex.colors
    var copied by remember { mutableStateOf(false) }

    if (message.role == Role.USER) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 22.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .background(
                        colors.bgUserBubble,
                        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message.content,
                    color = colors.text,
                    fontSize = 15.sp,
                    lineHeight = 23.sp
                )
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth().padding(top = 26.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SparkleLogo(size = 22.dp, tint = if (message.isError) colors.textTertiary else colors.accent)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = if (message.isError) "Error" else "Kynex AI",
                    fontSize = 13.sp,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(10.dp))

            if (message.isError) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.bgHover)
                        .padding(14.dp)
                ) {
                    Text(
                        text = message.content,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    RetryPill(onRetry)
                }
            } else {
                MarkdownText(markdown = message.content)
            }

            Spacer(Modifier.height(6.dp))
            Row {
                var feedback by remember(message.id) { mutableStateOf<Int>(0) }
                ActionIcon(
                    icon = if (copied) Icons.Rounded.Done else Icons.Rounded.ContentCopy,
                    contentDescription = "Copy message",
                    tint = if (copied) colors.accent else colors.textTertiary
                ) {
                    onCopy(message.content)
                    copied = true
                }
                if (isLastAssistant) {
                    ActionIcon(
                        icon = Icons.Rounded.Refresh,
                        contentDescription = "Regenerate response",
                        tint = colors.textTertiary,
                        onClick = onRegenerate
                    )
                }
                if (!message.isError) {
                    ActionIcon(
                        icon = Icons.Rounded.ThumbUp,
                        contentDescription = "Good response",
                        tint = if (feedback == 1) colors.accent else colors.textTertiary
                    ) { feedback = if (feedback == 1) 0 else 1 }
                    ActionIcon(
                        icon = Icons.Rounded.ThumbDown,
                        contentDescription = "Bad response",
                        tint = if (feedback == -1) colors.accent else colors.textTertiary
                    ) { feedback = if (feedback == -1) 0 else -1 }
                }
            }
        }
    }
}

@Composable
private fun RetryPill(onRetry: () -> Unit) {
    val colors = Kynex.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(colors.accent.copy(alpha = 0.12f))
            .clickable(onClick = onRetry)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Rounded.Refresh,
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text("Retry", color = colors.accent, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ActionIcon(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit
) {
    val colors = Kynex.colors
    IconButton(onClick = onClick, modifier = Modifier.size(38.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
    }
}

/** Chat input bar matching demo.html: rounded box, auto-grow text, accent send button. */
@Composable
fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    isStreaming: Boolean
) {
    val colors = Kynex.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .padding(bottom = 10.dp, top = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.bgInput, RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 26.dp, max = 160.dp)
            ) {
                if (value.isEmpty()) {
                    Text(
                        "How can I help you today?",
                        color = colors.textTertiary,
                        fontSize = 15.sp
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(color = colors.text, fontSize = 15.sp, lineHeight = 22.sp),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.accent),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Input tools (attach + mic), matching demo.html
                InputToolIcon(icon = Icons.Rounded.AttachFile, contentDescription = "Attach file") {}
                InputToolIcon(icon = Icons.Rounded.Mic, contentDescription = "Voice input") {}
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(
                            when {
                                isStreaming -> colors.bgHover
                                value.isNotBlank() -> colors.accent
                                else -> colors.accent.copy(alpha = 0.35f)
                            }
                        )
                        .clickable(enabled = isStreaming || value.isNotBlank()) {
                            if (isStreaming) onStop() else onSend()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isStreaming) Icons.Rounded.Stop else Icons.AutoMirrored.Rounded.Send,
                        contentDescription = if (isStreaming) "Stop generating" else "Send message",
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InputToolIcon(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val colors = Kynex.colors
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.textTertiary,
            modifier = Modifier.size(19.dp)
        )
    }
}

/** Top-bar model selector dropdown. */
@Composable
fun ModelSelector(
    selected: AiModel,
    models: List<AiModel>,
    onSelect: (AiModel) -> Unit
) {
    val colors = Kynex.colors
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selected.displayName,
                color = colors.text,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Rounded.ArrowDropUp,
                contentDescription = "Select model",
                tint = colors.textTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = colors.bgInput
        ) {
            models.forEach { model ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                model.displayName,
                                color = colors.text,
                                fontSize = 14.sp,
                                fontWeight = if (model.id == selected.id) FontWeight.SemiBold else FontWeight.Normal
                            )
                            Text(
                                model.provider,
                                color = colors.textTertiary,
                                fontSize = 11.sp
                            )
                        }
                    },
                    trailingIcon = {
                        if (model.id == selected.id) {
                            Icon(Icons.Rounded.Done, null, tint = colors.accent, modifier = Modifier.size(15.dp))
                        }
                    },
                    onClick = {
                        onSelect(model)
                        expanded = false
                    }
                )
            }
        }
    }
}

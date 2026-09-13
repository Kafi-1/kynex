package com.kynex.ai.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kynex.ai.core.theme.Kynex

private sealed class MdBlock {
    data class Paragraph(val text: String) : MdBlock()
    data class Heading(val level: Int, val text: String) : MdBlock()
    data class Code(val language: String, val code: String) : MdBlock()
    data class Bullet(val items: List<String>) : MdBlock()
    data class Numbered(val items: List<String>) : MdBlock()
    data class Quote(val text: String) : MdBlock()
}

private fun parseBlocks(md: String): List<MdBlock> {
    val blocks = mutableListOf<MdBlock>()
    val lines = md.lines()
    var i = 0
    val paragraph = StringBuilder()

    fun flushParagraph() {
        val text = paragraph.toString().trim()
        if (text.isNotEmpty()) blocks.add(MdBlock.Paragraph(text))
        paragraph.clear()
    }

    while (i < lines.size) {
        val line = lines[i]

        if (line.trimStart().startsWith("```")) {
            flushParagraph()
            val language = line.trimStart().removePrefix("```").trim()
            val code = StringBuilder()
            i++
            while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                code.appendLine(lines[i])
                i++
            }
            i++ // skip closing fence
            blocks.add(MdBlock.Code(language, code.toString().trimEnd()))
            continue
        }

        val trimmed = line.trim()
        val headingMatch = Regex("^(#{1,6})\\s+(.*)$").matchEntire(trimmed)
        if (headingMatch != null) {
            flushParagraph()
            blocks.add(MdBlock.Heading(headingMatch.groupValues[1].length, headingMatch.groupValues[2]))
            i++
            continue
        }

        if (trimmed.startsWith("> ")) {
            flushParagraph()
            val quote = StringBuilder(trimmed.removePrefix("> "))
            i++
            while (i < lines.size && lines[i].trim().startsWith("> ")) {
                quote.append(' ').append(lines[i].trim().removePrefix("> "))
                i++
            }
            blocks.add(MdBlock.Quote(quote.toString()))
            continue
        }

        val bullets = Regex("^[-*•]\\s+(.*)$")
        val bulletItems = mutableListOf<String>()
        while (i < lines.size && bullets.matches(lines[i].trim())) {
            bulletItems.add(bullets.matchEntire(lines[i].trim())!!.groupValues[1])
            i++
        }
        if (bulletItems.isNotEmpty()) {
            flushParagraph()
            blocks.add(MdBlock.Bullet(bulletItems))
            continue
        }

        val numbered = Regex("^\\d+\\.\\s+(.*)$")
        val numberedItems = mutableListOf<String>()
        while (i < lines.size && numbered.matches(lines[i].trim())) {
            numberedItems.add(numbered.matchEntire(lines[i].trim())!!.groupValues[1])
            i++
        }
        if (numberedItems.isNotEmpty()) {
            flushParagraph()
            blocks.add(MdBlock.Numbered(numberedItems))
            continue
        }

        if (trimmed.isEmpty()) {
            flushParagraph()
        } else {
            if (paragraph.isNotEmpty()) paragraph.append('\n')
            paragraph.append(trimmed)
        }
        i++
    }
    flushParagraph()
    return blocks
}

/** Inline markdown: **bold**, *italic*, `code`. */
private fun inlineMd(text: String, baseColor: androidx.compose.ui.graphics.Color): AnnotatedString =
    buildAnnotatedString {
        var i = 0
        fun appendPlain(s: String) = append(s)
        while (i < text.length) {
            when {
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end > 0) {
                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = baseColor))
                        appendPlain(text.substring(i + 2, end))
                        pop()
                        i = end + 2
                    } else {
                        appendPlain(text[i].toString()); i++
                    }
                }
                text[i] == '*' -> {
                    val end = text.indexOf('*', i + 1)
                    if (end > 0) {
                        pushStyle(SpanStyle(fontStyle = FontStyle.Italic, color = baseColor))
                        appendPlain(text.substring(i + 1, end))
                        pop()
                        i = end + 1
                    } else {
                        appendPlain(text[i].toString()); i++
                    }
                }
                text[i] == '`' -> {
                    val end = text.indexOf('`', i + 1)
                    if (end > 0) {
                        pushStyle(SpanStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp))
                        appendPlain(text.substring(i + 1, end))
                        pop()
                        i = end + 1
                    } else {
                        appendPlain(text[i].toString()); i++
                    }
                }
                else -> {
                    appendPlain(text[i].toString()); i++
                }
            }
        }
    }

@Composable
fun MarkdownText(markdown: String, modifier: Modifier = Modifier) {
    val colors = Kynex.colors
    val blocks = remember(markdown) { parseBlocks(markdown) }

    SelectionContainer {
        Column(modifier = modifier) {
            blocks.forEach { block ->
                when (block) {
                    is MdBlock.Heading -> Text(
                        text = inlineMd(block.text, colors.text),
                        color = colors.text,
                        fontSize = when (block.level) {
                            1 -> 22.sp; 2 -> 19.sp; 3 -> 17.sp; else -> 15.sp
                        },
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
                    )
                    is MdBlock.Paragraph -> Text(
                        text = inlineMd(block.text, colors.text),
                        color = colors.text,
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    is MdBlock.Quote -> Row(
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Box(
                            Modifier
                                .width(3.dp)
                                .height(20.dp)
                                .background(colors.accent, RoundedCornerShape(2.dp))
                        )
                        Text(
                            text = inlineMd(block.text, colors.textSecondary),
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                    is MdBlock.Bullet -> Column(Modifier.padding(bottom = 8.dp)) {
                        block.items.forEach { item ->
                            Row(modifier = Modifier.padding(bottom = 4.dp)) {
                                Text("•  ", color = colors.accent, fontSize = 15.sp)
                                Text(
                                    text = inlineMd(item, colors.text),
                                    fontSize = 15.sp,
                                    lineHeight = 24.sp
                                )
                            }
                        }
                    }
                    is MdBlock.Numbered -> Column(Modifier.padding(bottom = 8.dp)) {
                        block.items.forEachIndexed { idx, item ->
                            Row(modifier = Modifier.padding(bottom = 4.dp)) {
                                Text("${idx + 1}.  ", color = colors.accent, fontSize = 15.sp)
                                Text(
                                    text = inlineMd(item, colors.text),
                                    fontSize = 15.sp,
                                    lineHeight = 24.sp
                                )
                            }
                        }
                    }
                    is MdBlock.Code -> CodeBlock(block.language, block.code)
                }
            }
        }
    }
}

@Composable
private fun CodeBlock(language: String, code: String) {
    val colors = Kynex.colors
    var copied by remember { mutableStateOf(false) }
    val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(colors.bgSidebar, RoundedCornerShape(10.dp))
            .padding(1.dp)
            .background(colors.bgSidebar, RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 2.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.ifBlank { "code" },
                color = colors.textTertiary,
                fontSize = 11.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                clipboard.setText(androidx.compose.ui.text.AnnotatedString(code))
                copied = true
            }) {
                Icon(
                    imageVector = if (copied) Icons.Rounded.Done else Icons.Rounded.ContentCopy,
                    contentDescription = "Copy code",
                    tint = if (copied) colors.accent else colors.textTertiary,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
        Text(
            text = code,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            lineHeight = 21.sp,
            color = colors.text,
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

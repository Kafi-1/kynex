package com.kynex.ai.presentation.screens.imagegen

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kynex.ai.core.theme.Kynex
import com.kynex.ai.di.AppGraph
import com.kynex.ai.presentation.VMFactory
import java.io.File

@Composable
fun ImageGenScreen(onBack: () -> Unit) {
    val vm: ImageGenViewModel = viewModel(factory = VMFactory(AppGraph))
    val state = vm.state
    val colors = Kynex.colors
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Top bar
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
                "Image Generate",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Prompt input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.bgInput)
                    .padding(16.dp)
            ) {
                if (state.prompt.isEmpty()) {
                    Text(
                        "Describe the image you want, in any language...\n" +
                            "e.g. \"Kynex naam e ekta AI app er jonno professional logo banao\"",
                        color = colors.textTertiary,
                        fontSize = 14.5.sp,
                        lineHeight = 21.sp
                    )
                }
                BasicTextField(
                    value = state.prompt,
                    onValueChange = vm::onPromptChange,
                    textStyle = TextStyle(color = colors.text, fontSize = 14.5.sp, lineHeight = 21.sp),
                    cursorBrush = SolidColor(colors.accent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 92.dp)
                )
            }

            Spacer(Modifier.height(14.dp))

            // Generate / Retry button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (state.canGenerate) colors.accent else colors.accent.copy(alpha = 0.35f)
                    )
                    .clickable(enabled = state.canGenerate) { vm.generate() },
                contentAlignment = Alignment.Center
            ) {
                if (state.isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.size(10.dp))
                        Text(
                            if (state.imageBytes == null) "Generating..." else "Retrying...",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(Modifier.size(8.dp))
                        Text(
                            "Generate",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Content area
            when {
                state.isLoading && state.imageBytes == null -> {
                    LoadingCard()
                }
                state.error != null -> {
                    ErrorCard(message = state.error ?: "", onRetry = vm::retry)
                }
                state.imageBytes != null -> {
                    ResultCard(
                        bytes = state.imageBytes!!,
                        onSave = { saveImage(context, state.imageBytes!!) },
                        onShare = { shareImage(context, state.imageBytes!!) },
                        onNewGeneration = { vm.reset() }
                    )
                }
                else -> {
                    EmptyCard()
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun EmptyCard() {
    val colors = Kynex.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.bgInput)
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Rounded.AutoAwesome,
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(36.dp)
        )
        Spacer(Modifier.height(14.dp))
        Text(
            "Your generated image will appear here",
            fontSize = 14.sp,
            color = colors.textTertiary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LoadingCard() {
    val colors = Kynex.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.bgInput)
            .padding(vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = colors.accent,
            strokeWidth = 3.dp
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Creating your image...",
            fontSize = 14.sp,
            color = colors.textSecondary
        )
        Text(
            "This can take up to a minute",
            fontSize = 12.sp,
            color = colors.textTertiary
        )
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    val colors = Kynex.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.bgInput)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Generation failed",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            fontSize = 13.5.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(colors.accent.copy(alpha = 0.12f))
                .clickable(onClick = onRetry)
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Refresh, contentDescription = null, tint = colors.accent, modifier = Modifier.size(15.dp))
            Spacer(Modifier.size(6.dp))
            Text("Retry", color = colors.accent, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ResultCard(
    bytes: ByteArray,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onNewGeneration: () -> Unit
) {
    val colors = Kynex.colors
    val bitmap = remember(bytes) { BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Generated image",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
        )
    }

    Spacer(Modifier.height(14.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
    ) {
        ActionPill(
            label = "Save",
            icon = Icons.Rounded.Save,
            modifier = Modifier.weight(1f),
            onClick = onSave
        )
        ActionPill(
            label = "Share",
            icon = Icons.Rounded.Share,
            modifier = Modifier.weight(1f),
            onClick = onShare
        )
    }

    Spacer(Modifier.height(10.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onNewGeneration)
            .padding(vertical = 12.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(15.dp))
        Spacer(Modifier.size(6.dp))
        Text("Generate another image", fontSize = 13.5.sp, color = colors.textSecondary)
    }
}

@Composable
private fun ActionPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = Kynex.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.bgInput)
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(16.dp))
        Spacer(Modifier.size(8.dp))
        Text(label, color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

// ---------- Save & Share helpers ----------

private fun saveImage(context: Context, bytes: ByteArray) {
    try {
        val name = "kynex_${System.currentTimeMillis()}.png"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Kynex AI")
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri == null) {
                Toast.makeText(context, "Could not save the image.", Toast.LENGTH_SHORT).show()
                return
            }
            context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
            Toast.makeText(context, "Saved to Pictures/Kynex AI", Toast.LENGTH_SHORT).show()
        } else {
            val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Kynex AI")
            dir.mkdirs()
            val file = File(dir, name)
            file.writeBytes(bytes)
            Toast.makeText(context, "Saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Could not save the image.", Toast.LENGTH_SHORT).show()
    }
}

private fun shareImage(context: Context, bytes: ByteArray) {
    try {
        val file = File(context.cacheDir, "kynex_share_${System.currentTimeMillis()}.png")
        file.writeBytes(bytes)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share image"))
    } catch (e: Exception) {
        Toast.makeText(context, "Could not share the image.", Toast.LENGTH_SHORT).show()
    }
}

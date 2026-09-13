package com.kynex.ai.presentation.screens.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kynex.ai.core.theme.Kynex
import com.kynex.ai.core.theme.SerifDisplay
import com.kynex.ai.presentation.components.SparkleLogo
import com.kynex.ai.presentation.screens.settings.SettingsCard
import com.kynex.ai.presentation.screens.settings.SettingsRowItem
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Code

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val colors = Kynex.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
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
            Text("About", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = colors.text)
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            SparkleLogo(size = 56.dp, tint = colors.accent)
            Spacer(Modifier.height(14.dp))
            Text(
                "Kynex AI",
                fontFamily = SerifDisplay,
                fontSize = 28.sp,
                color = colors.text
            )
            Text("Version 1.0.0", fontSize = 13.sp, color = colors.textTertiary)
            Spacer(Modifier.height(20.dp))
            Text(
                "A multi-model AI chat assistant.\n" +
                    "Choose from multiple AI models, keep your history in sync\n" +
                    "and pick up conversations on any device.",
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = colors.textSecondary,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }

        Spacer(Modifier.height(28.dp))
        SettingsCard {
            SettingsRowItem(
                icon = Icons.Rounded.SmartToy,
                title = "AI models",
                subtitle = "GPT-5.6-Sol, Claude Opus, DeepSeek V4 Flash, GLM",
                onClick = {}
            )
            SettingsRowItem(
                icon = Icons.Rounded.Key,
                title = "AI provider",
                subtitle = "AgentRouter API",
                onClick = {}
            )
            SettingsRowItem(
                icon = Icons.Rounded.Storage,
                title = "Account & data",
                subtitle = "Firebase Authentication & Cloud Firestore",
                onClick = {}
            )
            SettingsRowItem(
                icon = Icons.Rounded.Code,
                title = "Built with",
                subtitle = "Kotlin, Jetpack Compose",
                onClick = {}
            )
        }

        Spacer(Modifier.height(32.dp))
        Text(
            "Made for personal use",
            fontSize = 12.sp,
            color = colors.textTertiary,
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

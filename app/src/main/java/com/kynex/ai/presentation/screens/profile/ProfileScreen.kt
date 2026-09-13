package com.kynex.ai.presentation.screens.profile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kynex.ai.core.theme.Kynex
import com.kynex.ai.di.AppGraph
import com.kynex.ai.presentation.VMFactory
import com.kynex.ai.presentation.screens.settings.SettingsCard
import com.kynex.ai.presentation.screens.settings.SettingsSectionLabel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(onBack: () -> Unit) {
    val vm: com.kynex.ai.presentation.screens.settings.AccountViewModel =
        viewModel(factory = VMFactory(AppGraph))
    val profile = vm.state.profile
    val colors = Kynex.colors

    LaunchedEffect(Unit) { vm.loadProfile() }

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
            Text("Profile", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = colors.text)
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(28.dp))
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(colors.accent, colors.accentHover))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (profile?.name ?: "U").trim().firstOrNull()?.uppercase() ?: "U",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                profile?.name ?: "",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = colors.text
            )
            Text(
                profile?.email ?: "",
                fontSize = 13.sp,
                color = colors.textTertiary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Free plan",
                fontSize = 12.sp,
                color = colors.accent,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colors.accent.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        SettingsSectionLabel("Display name")
        SettingsCard {
            var name by remember(profile?.name) { mutableStateOf(profile?.name ?: "") }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    textStyle = TextStyle(color = colors.text, fontSize = 15.sp),
                    cursorBrush = SolidColor(colors.accent),
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { vm.updateName(name) }, enabled = name.isNotBlank()) {
                    Icon(Icons.Rounded.Edit, null, tint = colors.accent, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Save", color = colors.accent)
                }
            }
        }

        SettingsSectionLabel("Account details")
        SettingsCard {
            ProfileInfoRow("Email", profile?.email ?: "—")
            ProfileInfoRow(
                "Sign-in provider",
                when (profile?.provider) {
                    "password" -> "Email & password"
                    "google.com" -> "Google"
                    "github.com" -> "GitHub"
                    else -> profile?.provider ?: "—"
                }
            )
            ProfileInfoRow(
                "Member since",
                if ((profile?.createdAt ?: 0L) > 0)
                    remember(profile?.createdAt) {
                        SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date(profile!!.createdAt))
                    }
                else "—"
            )
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    val colors = Kynex.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(label, fontSize = 14.sp, color = colors.textSecondary, modifier = Modifier.weight(1f))
        Text(value, fontSize = 14.sp, color = colors.text)
    }
}

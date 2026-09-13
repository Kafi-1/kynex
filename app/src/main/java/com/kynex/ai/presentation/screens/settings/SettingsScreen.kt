package com.kynex.ai.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kynex.ai.core.theme.Kynex
import com.kynex.ai.data.network.AiConfig
import com.kynex.ai.di.AppGraph
import com.kynex.ai.presentation.VMFactory

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val vm: AccountViewModel = viewModel(factory = VMFactory(AppGraph))
    val state = vm.state
    val colors = Kynex.colors
    var modelMenuOpen by remember { mutableStateOf(false) }
    var confirmLogout by remember { mutableStateOf(false) }

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
            Text("Settings", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = colors.text)
        }

        SettingsSectionLabel("AI Model")
        SettingsCard {
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { modelMenuOpen = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.SmartToy, null, tint = colors.accent, modifier = Modifier.size(19.dp))
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Default model", fontSize = 15.sp, color = colors.text)
                        Text(
                            state.selectedModel?.displayName ?: AiConfig.DEFAULT_MODEL.displayName,
                            fontSize = 12.sp,
                            color = colors.textTertiary
                        )
                    }
                    Icon(Icons.Rounded.ArrowDropUp, "Change model", tint = colors.textTertiary)
                }
                DropdownMenu(
                    expanded = modelMenuOpen,
                    onDismissRequest = { modelMenuOpen = false },
                    containerColor = colors.bgInput
                ) {
                    AiConfig.MODELS.forEach { model ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(model.displayName, color = colors.text, fontSize = 14.sp)
                                    Text(model.provider, color = colors.textTertiary, fontSize = 11.sp)
                                }
                            },
                            trailingIcon = {
                                if (model.id == state.selectedModel?.id) {
                                    Icon(Icons.Rounded.Done, null, tint = colors.accent, modifier = Modifier.size(15.dp))
                                }
                            },
                            onClick = {
                                vm.selectModel(model)
                                modelMenuOpen = false
                            }
                        )
                    }
                }
            }
        }

        SettingsSectionLabel("Appearance")
        SettingsCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { vm.toggleTheme() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.DarkMode, null, tint = colors.accent, modifier = Modifier.size(19.dp))
                Spacer(Modifier.size(12.dp))
                Text("Dark theme", fontSize = 15.sp, color = colors.text, modifier = Modifier.weight(1f))
                ToggleSwitch(checked = state.isDarkTheme, onCheckedChange = { vm.toggleTheme() })
            }
        }

        SettingsSectionLabel("Account")
        SettingsCard {
            SettingsRowItem(icon = Icons.Rounded.Person, title = "Profile", subtitle = state.profile?.email ?: "", onClick = onNavigateToProfile)
            SettingsRowItem(icon = Icons.Rounded.Info, title = "About", subtitle = "Kynex AI v1.0.0", onClick = onNavigateToAbout)
            SettingsRowItem(
                icon = Icons.AutoMirrored.Rounded.Logout,
                title = "Log out",
                subtitle = "",
                onClick = { confirmLogout = true },
                tint = colors.accent
            )
        }

        Spacer(Modifier.height(40.dp))
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text("Log out?") },
            text = { Text("You will need to sign in again to access your chats.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmLogout = false
                    vm.logout()
                }) { Text("Log out", color = colors.accent) }
            },
            dismissButton = {
                TextButton(onClick = { confirmLogout = false }) { Text("Cancel") }
            },
            containerColor = colors.bgInput
        )
    }
}

@Composable
internal fun SettingsSectionLabel(text: String) {
    val colors = Kynex.colors
    Text(
        text,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.SemiBold,
        color = colors.textTertiary,
        modifier = Modifier.padding(start = 30.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
internal fun SettingsCard(content: @Composable () -> Unit) {
    val colors = Kynex.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.bgInput)
    ) {
        content()
    }
}

@Composable
internal fun SettingsRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color? = null
) {
    val colors = Kynex.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint ?: colors.accent, modifier = Modifier.size(19.dp))
        Spacer(Modifier.size(12.dp))
        Column {
            Text(title, fontSize = 15.sp, color = tint ?: colors.text)
            if (subtitle.isNotBlank()) {
                Text(subtitle, fontSize = 12.sp, color = colors.textTertiary)
            }
        }
    }
}

@Composable
private fun ToggleSwitch(checked: Boolean, onCheckedChange: () -> Unit) {
    val colors = Kynex.colors
    Box(
        modifier = Modifier
            .size(width = 44.dp, height = 26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(if (checked) colors.accent else colors.bgHover)
            .clickable(onClick = onCheckedChange),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 3.dp)
                .size(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(androidx.compose.ui.graphics.Color.White)
        )
    }
}

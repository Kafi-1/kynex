package com.kynex.ai.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kynex.ai.core.theme.KynexTheme
import com.kynex.ai.data.prefs.SettingsDataStore
import com.kynex.ai.di.AppGraph
import com.kynex.ai.presentation.navigation.KynexNavHost

@Composable
fun KynexRoot() {
    val theme by AppGraph.settingsStore.themeFlow
        .collectAsStateWithLifecycle(initialValue = SettingsDataStore.THEME_LIGHT)

    // Show the last crash report so it can be copied and reported.
    var crashText by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { crashText = AppGraph.readCrashLog() }
    val clipboard = LocalClipboardManager.current

    KynexTheme(darkTheme = theme == SettingsDataStore.THEME_DARK) {
        KynexNavHost()

        crashText?.let { crash ->
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Last crash report") },
                text = {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            crash.take(3000),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { clipboard.setText(AnnotatedString(crash)) }) {
                        Text("Copy")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        AppGraph.clearCrashLog()
                        crashText = null
                    }) { Text("Dismiss") }
                }
            )
        }
    }
}

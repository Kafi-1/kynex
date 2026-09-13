package com.kynex.ai.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kynex.ai.core.theme.KynexTheme
import com.kynex.ai.data.prefs.SettingsDataStore
import com.kynex.ai.di.AppGraph
import com.kynex.ai.presentation.navigation.KynexNavHost

@Composable
fun KynexRoot() {
    val theme by AppGraph.settingsStore.themeFlow
        .collectAsStateWithLifecycle(initialValue = SettingsDataStore.THEME_LIGHT)

    KynexTheme(darkTheme = theme == SettingsDataStore.THEME_DARK) {
        KynexNavHost()
    }
}

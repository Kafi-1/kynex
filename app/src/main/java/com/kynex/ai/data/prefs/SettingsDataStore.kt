package com.kynex.ai.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "kynex_settings")

/** Lightweight local preferences only — permanent chat data lives in Firestore. */
class SettingsDataStore(private val context: Context) {

    companion object {
        val THEME = stringPreferencesKey("theme")            // "light" | "dark"
        val LAST_MODEL = stringPreferencesKey("last_model")  // API model id
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
    }

    val themeFlow: Flow<String> =
        context.dataStore.data.map { it[THEME] ?: THEME_LIGHT }

    val lastModelFlow: Flow<String> =
        context.dataStore.data.map { it[LAST_MODEL] ?: "" }

    suspend fun setTheme(value: String) {
        context.dataStore.edit { it[THEME] = value }
    }

    suspend fun setLastModel(modelId: String) {
        context.dataStore.edit { it[LAST_MODEL] = modelId }
    }
}

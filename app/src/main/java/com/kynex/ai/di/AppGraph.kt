package com.kynex.ai.di

import android.content.Context
import com.kynex.ai.data.auth.AuthRepository
import com.kynex.ai.data.firestore.ChatRepository
import com.kynex.ai.data.network.AgentRouterService
import com.kynex.ai.data.network.PollinationsImageService
import com.kynex.ai.data.prefs.SettingsDataStore

/** Simple manual dependency container. */
object AppGraph {

    lateinit var settingsStore: SettingsDataStore
        private set

    val authService = AgentRouterService()
    val imageService = PollinationsImageService()
    val authRepository: AuthRepository by lazy { AuthRepository(appContext) }

    private lateinit var appContext: Context

    fun init(context: Context) {
        if (::appContext.isInitialized) return
        appContext = context.applicationContext
        settingsStore = SettingsDataStore(appContext)
    }

    fun chatRepository(uid: String): ChatRepository = ChatRepository(uid)
}

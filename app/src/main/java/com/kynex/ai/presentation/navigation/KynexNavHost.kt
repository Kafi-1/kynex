package com.kynex.ai.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kynex.ai.di.AppGraph
import com.kynex.ai.presentation.screens.about.AboutScreen
import com.kynex.ai.presentation.screens.auth.LoginScreen
import com.kynex.ai.presentation.screens.auth.SignupScreen
import com.kynex.ai.presentation.screens.chat.ChatScreen
import com.kynex.ai.presentation.screens.history.HistoryScreen
import com.kynex.ai.presentation.screens.imagegen.ImageGenScreen
import com.kynex.ai.presentation.screens.saved.SavedScreen
import com.kynex.ai.presentation.screens.settings.SettingsScreen
import com.kynex.ai.presentation.screens.profile.ProfileScreen
import com.kynex.ai.presentation.screens.splash.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val CHAT = "chat?chatId={chatId}"
    const val HISTORY = "history"
    const val SAVED = "saved"
    const val IMAGEGEN = "imagegen"
    const val SETTINGS = "settings"
    const val PROFILE = "profile"
    const val ABOUT = "about"

    fun chat(chatId: String = "") = "chat?chatId=$chatId"
}

@Composable
fun KynexNavHost() {
    val navController = rememberNavController()
    val uid by AppGraph.authRepository.authStateFlow()
        .collectAsStateWithLifecycle(initialValue = AppGraph.authRepository.currentUserId())

    // Auto-return to login when the user signs out from anywhere.
    LaunchedEffect(uid) {
        if (uid == null) {
            val current = navController.currentDestination?.route
            if (current != null && current !in listOf(Routes.SPLASH, Routes.LOGIN, Routes.SIGNUP)) {
                navController.navigate(Routes.LOGIN) { popUpTo(0) }
            }
        }
    }

    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onLoggedIn = { navController.navigate(Routes.chat()) { popUpTo(0) } },
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) { popUpTo(0) }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToChat = { navController.navigate(Routes.chat()) { popUpTo(0) } },
                onNavigateToSignup = { navController.navigate(Routes.SIGNUP) }
            )
        }

        composable(Routes.SIGNUP) {
            SignupScreen(
                onNavigateToChat = { navController.navigate(Routes.chat()) { popUpTo(0) } },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.CHAT,
            arguments = listOf(navArgument("chatId") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { entry ->
            ChatScreen(
                openChatId = entry.arguments?.getString("chatId").orEmpty(),
                onNavigateToHistory = { navController.navigate(Routes.HISTORY) },
                onNavigateToSaved = { navController.navigate(Routes.SAVED) },
                onNavigateToImageGen = { navController.navigate(Routes.IMAGEGEN) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                onOpenChat = { id -> navController.navigate(Routes.chat(id)) { launchSingleTop = true } }
            )
        }

        composable(Routes.IMAGEGEN) {
            ImageGenScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                onOpenChat = { id ->
                    navController.navigate(Routes.chat(id)) { launchSingleTop = true }
                }
            )
        }

        composable(Routes.SAVED) {
            SavedScreen(
                onBack = { navController.popBackStack() },
                onOpenChat = { id ->
                    navController.navigate(Routes.chat(id)) { launchSingleTop = true }
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAbout = { navController.navigate(Routes.ABOUT) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}

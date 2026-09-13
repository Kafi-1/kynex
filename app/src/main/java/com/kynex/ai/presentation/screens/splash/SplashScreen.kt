package com.kynex.ai.presentation.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kynex.ai.core.theme.Kynex
import com.kynex.ai.core.theme.SerifDisplay
import com.kynex.ai.di.AppGraph
import com.kynex.ai.presentation.components.SparkleLogo
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onLoggedIn: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val uid by AppGraph.authRepository.authStateFlow()
        .collectAsStateWithLifecycle(initialValue = AppGraph.authRepository.currentUserId())

    LaunchedEffect(uid) {
        delay(900)
        if (uid != null) onLoggedIn() else onLoggedOut()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Kynex.colors.bg),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SparkleLogo(size = 56.dp, tint = Kynex.colors.accent)
            Text(
                text = "Kynex AI",
                fontFamily = SerifDisplay,
                fontSize = 32.sp,
                color = Kynex.colors.text
            )
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp).align(Alignment.CenterHorizontally),
                color = Kynex.colors.accent,
                strokeWidth = 2.dp,
                trackColor = Color.Transparent
            )
        }
    }
}

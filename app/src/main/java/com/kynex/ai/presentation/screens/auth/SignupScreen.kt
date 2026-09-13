package com.kynex.ai.presentation.screens.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kynex.ai.core.theme.Kynex
import com.kynex.ai.core.theme.SerifDisplay
import com.kynex.ai.di.AppGraph
import com.kynex.ai.presentation.VMFactory
import com.kynex.ai.presentation.components.AuthButton
import com.kynex.ai.presentation.components.AuthField
import com.kynex.ai.presentation.components.SocialButton
import com.kynex.ai.presentation.components.SparkleLogo

@Composable
fun SignupScreen(
    onNavigateToChat: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val vm: AuthViewModel = viewModel(factory = VMFactory(AppGraph))
    val state = vm.state
    val uid by vm.authState.collectAsStateWithLifecycle(initialValue = null)
    val activity = LocalContext.current as? Activity
    val colors = Kynex.colors

    LaunchedEffect(uid) {
        if (uid != null) onNavigateToChat()
    }

    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> vm.handleGoogleResult(result.data) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(64.dp))
        SparkleLogo(size = 52.dp, tint = colors.accent)
        Spacer(Modifier.height(18.dp))
        Text(
            "Create your account",
            fontFamily = SerifDisplay,
            fontSize = 28.sp,
            color = colors.text
        )
        Spacer(Modifier.height(32.dp))

        AuthField(
            value = state.name,
            onValueChange = vm::onNameChange,
            label = "Name",
            leadingIcon = Icons.Rounded.Person
        )
        Spacer(Modifier.height(12.dp))
        AuthField(
            value = state.email,
            onValueChange = vm::onEmailChange,
            label = "Email",
            leadingIcon = Icons.Rounded.Email
        )
        Spacer(Modifier.height(12.dp))
        AuthField(
            value = state.password,
            onValueChange = vm::onPasswordChange,
            label = "Password (min 6 characters)",
            isPassword = true,
            leadingIcon = Icons.Rounded.Lock
        )

        if (state.error != null) {
            Spacer(Modifier.height(12.dp))
            Text(state.error!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        Spacer(Modifier.height(24.dp))
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(26.dp),
                color = colors.accent,
                strokeWidth = 2.dp
            )
        } else {
            AuthButton("Sign up", vm::signUp)
        }

        Spacer(Modifier.height(28.dp))
        Row {
            SocialButton("Google", Modifier) {
                googleLauncher.launch(AppGraph.authRepository.googleSignInIntent())
            }
            Spacer(Modifier.size(12.dp))
            SocialButton("GitHub", Modifier) {
                activity?.let { vm.signInWithGitHub(it) }
            }
        }

        Spacer(Modifier.height(32.dp))
        Text(
            "Already have an account? Log in",
            fontSize = 14.sp,
            color = colors.accent,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToLogin() }
                .padding(10.dp)
        )
        Spacer(Modifier.height(24.dp))
    }
}

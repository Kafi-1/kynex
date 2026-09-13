package com.kynex.ai.presentation.screens.auth

import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynex.ai.di.AppGraph
import kotlinx.coroutines.launch

data class AuthUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val info: String? = null
)

class AuthViewModel(private val graph: AppGraph) : ViewModel() {

    var state by mutableStateOf(AuthUiState())
        private set

    val authState = graph.authRepository.authStateFlow()

    fun onNameChange(v: String) { state = state.copy(name = v, error = null) }
    fun onEmailChange(v: String) { state = state.copy(email = v, error = null) }
    fun onPasswordChange(v: String) { state = state.copy(password = v, error = null) }

    fun signIn() {
        if (state.isLoading) return
        if (state.email.isBlank() || state.password.isBlank()) {
            state = state.copy(error = "Please enter your email and password.")
            return
        }
        launchAuth { graph.authRepository.signIn(state.email.trim(), state.password) }
    }

    fun signUp() {
        if (state.isLoading) return
        if (state.name.isBlank() || state.email.isBlank() || state.password.length < 6) {
            state = state.copy(error = "Enter a name, email and a password of at least 6 characters.")
            return
        }
        launchAuth {
            graph.authRepository.signUp(state.name.trim(), state.email.trim(), state.password)
        }
    }

    fun resetPassword() {
        if (state.email.isBlank()) {
            state = state.copy(error = "Enter your email first, then tap reset.")
            return
        }
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            val result = graph.authRepository.sendPasswordReset(state.email.trim())
            state = state.copy(
                isLoading = false,
                info = if (result.isSuccess) "Password reset email sent." else null,
                error = result.exceptionOrNull()?.let(::friendly)
            )
        }
    }

    fun handleGoogleResult(data: Intent?) {
        launchAuth { graph.authRepository.handleGoogleResult(data) }
    }

    fun signInWithGitHub(activity: Activity) {
        launchAuth { graph.authRepository.signInWithGitHub(activity) }
    }

    private fun launchAuth(block: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null, info = null)
            val result = block()
            state = state.copy(
                isLoading = false,
                error = result.exceptionOrNull()?.let(::friendly)
            )
        }
    }

    private fun friendly(e: Throwable): String = when {
        e.message?.contains("network", ignoreCase = true) == true ->
            "No internet connection. Please check and try again."
        e.message?.contains("password is invalid", ignoreCase = true) == true ||
            e.message?.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) == true ->
            "Incorrect email or password."
        e.message?.contains("email address is already in use", ignoreCase = true) == true ->
            "This email is already registered. Try logging in."
        e.message?.contains("badly formatted", ignoreCase = true) == true ->
            "That email address doesn't look right."
        else -> e.message ?: "Something went wrong. Please try again."
    }
}

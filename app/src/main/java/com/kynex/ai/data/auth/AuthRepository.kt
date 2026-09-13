package com.kynex.ai.data.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GithubAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * CENTRAL Firebase Authentication configuration for social sign-in.
 * TODO(owner): paste your Web Client ID from Firebase Console → Authentication → Sign-in method → Google.
 */
object AuthConfig {
    const val GOOGLE_WEB_CLIENT_ID =
        "121274973056-99rfhhhisue1k1v2l1i94956sksa6aas.apps.googleusercontent.com"
}

class AuthRepository(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun currentUserId(): String? = auth.currentUser?.uid

    fun currentUserName(): String =
        auth.currentUser?.displayName ?: auth.currentUser?.email?.substringBefore("@") ?: "User"

    fun authStateFlow(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.uid)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signUp(name: String, email: String, password: String): Result<Unit> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        result.user?.updateProfile(
            userProfileChangeRequest { displayName = name.ifBlank { email.substringBefore("@") } }
        )?.await()
        ensureProfile(result.user)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun signIn(email: String, password: String): Result<Unit> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        ensureProfile(result.user)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun googleSignInIntent(): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(AuthConfig.GOOGLE_WEB_CLIENT_ID)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso).signInIntent
    }

    suspend fun handleGoogleResult(data: Intent?): Result<Unit> = try {
        val account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)
        val idToken = account?.idToken
            ?: return Result.failure(IllegalStateException("Google sign-in returned no ID token"))
        signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun signInWithGitHub(activity: Activity): Result<Unit> = try {
        val provider = OAuthProvider.newBuilder("github.com").build()
        val result = auth.startActivityForSignInWithProvider(activity, provider).await()
        ensureProfile(result.user)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun signInWithCredential(credential: AuthCredential): Result<Unit> = try {
        val result = auth.signInWithCredential(credential).await()
        ensureProfile(result.user)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /** Creates users/{uid} profile document on first sign-in. Never stores passwords. */
    private suspend fun ensureProfile(user: FirebaseUser?) {
        user ?: return
        val ref = db.collection("users").document(user.uid)
        val existing = ref.get().await()
        if (!existing.exists()) {
            val now = System.currentTimeMillis()
            ref.set(
                mapOf(
                    "name" to (user.displayName ?: user.email?.substringBefore("@") ?: "User"),
                    "email" to (user.email ?: ""),
                    "photoUrl" to (user.photoUrl?.toString() ?: ""),
                    "provider" to (user.providerData.lastOrNull()?.providerId ?: "password"),
                    "createdAt" to now,
                    "updatedAt" to now
                )
            ).await()
        }
    }

    fun signOut() {
        GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
        auth.signOut()
    }
}

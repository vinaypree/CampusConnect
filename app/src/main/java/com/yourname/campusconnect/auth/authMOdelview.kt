package com.yourname.campusconnect.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object Authenticated : AuthState()
        data class Message(val message: String) : AuthState()
        data class Error(val message: String) : AuthState()
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    /** 🔹 Logout user */
    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Message("Logged out")
    }

    /** 🔹 Sign up new user + send verification email */
    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                if (email.isBlank() || password.isBlank()) {
                    _authState.value = AuthState.Error("Email and password cannot be empty.")
                    return@launch
                }
                if (!email.endsWith("@iitrpr.ac.in")) {
                    _authState.value =
                        AuthState.Error("Please use your campus email (@iitrpr.ac.in).")
                    return@launch
                }

                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user
                user?.sendEmailVerification()?.await()

                _authState.value = AuthState.Message(
                    "Verification email sent! Please verify before logging in."
                )
            } catch (e: FirebaseAuthUserCollisionException) {
                _authState.value =
                    AuthState.Error("An account with this email already exists.")
            } catch (e: Exception) {
                _authState.value =
                    AuthState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }

    /** 🔹 Log in existing user */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                if (email.isBlank() || password.isBlank()) {
                    _authState.value =
                        AuthState.Error("Email and password cannot be empty.")
                    return@launch
                }

                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user
                if (user != null && user.isEmailVerified) {
                    _authState.value = AuthState.Authenticated
                } else {
                    auth.signOut()
                    _authState.value =
                        AuthState.Error("Please verify your email before logging in.")
                }
            } catch (e: Exception) {
                _authState.value =
                    AuthState.Error(e.message ?: "Invalid email or password.")
            }
        }
    }

    /** 🔹 Send reset password link */
    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                if (email.isBlank()) {
                    _authState.value = AuthState.Error("Email cannot be empty.")
                    return@launch
                }
                auth.sendPasswordResetEmail(email).await()
                _authState.value =
                    AuthState.Message("Password reset link sent to your email.")
            } catch (e: Exception) {
                _authState.value =
                    AuthState.Error(e.message ?: "Failed to send reset email.")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

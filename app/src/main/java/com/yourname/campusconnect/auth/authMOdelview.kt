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
        object Unauthenticated : AuthState()
        data class Error(val message: String) : AuthState()
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    // --- THIS FUNCTION WAS MISSING ITS CODE ---
    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                if (email.isBlank() || password.isBlank()) {
                    _authState.value = AuthState.Error("Email and password cannot be empty.")
                    return@launch
                }
                if (!email.endsWith("@iitrpr.ac.in")) { // Replace with your college's domain
                    _authState.value = AuthState.Error("Please use your campus email (@iitrpr.ac.in).")
                    return@launch
                }
                auth.createUserWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Authenticated
            } catch (e: FirebaseAuthUserCollisionException) {
                _authState.value = AuthState.Error("An account with this email already exists.")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }

    // --- THIS FUNCTION WAS ALSO MISSING ITS CODE ---
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                if (email.isBlank() || password.isBlank()) {
                    _authState.value = AuthState.Error("Email and password cannot be empty.")
                    return@launch
                }
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Invalid email or password.")
            }
        }
    }


    fun resetState() {
        _authState.value = AuthState.Idle
    }


}
package com.yourname.campusconnect.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.yourname.campusconnect.data.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository()

    sealed class UserStatus {
        object Loading : UserStatus()
        object LoggedOut : UserStatus()
        object NeedsProfile : UserStatus()
        object LoggedIn : UserStatus()
    }

    private val _userStatus = MutableStateFlow<UserStatus>(UserStatus.Loading)
    val userStatus: StateFlow<UserStatus> = _userStatus

    init {
        checkUserStatus()
    }

    private fun checkUserStatus() {
        viewModelScope.launch {
            delay(500)

            val currentUser = auth.currentUser
            if (currentUser == null) {
                _userStatus.value = UserStatus.LoggedOut
            } else {
                if (userRepository.hasUserProfile()) {
                    _userStatus.value = UserStatus.LoggedIn
                } else {
                    _userStatus.value = UserStatus.NeedsProfile
                }
            }
        }
    }
}


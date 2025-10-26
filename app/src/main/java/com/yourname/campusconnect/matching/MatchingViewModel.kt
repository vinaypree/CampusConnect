package com.yourname.campusconnect.matching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.campusconnect.data.models.User
import com.yourname.campusconnect.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MatchingViewModel : ViewModel() {

    private val userRepository = UserRepository()

    sealed class UserListState {
        object Loading : UserListState()
        data class Success(val users: List<User>) : UserListState()
        data class Error(val message: String) : UserListState()
    }

    private val _userListState = MutableStateFlow<UserListState>(UserListState.Loading)
    val userListState: StateFlow<UserListState> = _userListState

    init {
        fetchAllUsers()
    }

    fun fetchAllUsers() {
        viewModelScope.launch {
            _userListState.value = UserListState.Loading
            val result = userRepository.getAllUsers()
            if (result.isSuccess) {
                _userListState.value = UserListState.Success(result.getOrNull() ?: emptyList())
            } else {
                _userListState.value = UserListState.Error(result.exceptionOrNull()?.message ?: "Failed to fetch users.")
            }
        }
    }
}


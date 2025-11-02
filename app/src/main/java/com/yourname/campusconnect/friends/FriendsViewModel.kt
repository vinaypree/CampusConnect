package com.yourname.campusconnect.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.campusconnect.data.models.User
import com.yourname.campusconnect.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FriendsViewModel : ViewModel() {
    private val userRepository = UserRepository()

    private val _friends = MutableStateFlow<List<User>>(emptyList())
    val friends: StateFlow<List<User>> = _friends

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchFriends()
    }


    fun fetchFriends() {
        viewModelScope.launch {
            _isLoading.value = true
            userRepository.getAcceptedFriends().onSuccess {
                _friends.value = it
            }.onFailure {
                _friends.value = emptyList()
            }
            _isLoading.value = false
        }
    }
}

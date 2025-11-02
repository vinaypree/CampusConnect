package com.yourname.campusconnect.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.campusconnect.data.models.User
import com.yourname.campusconnect.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Data class to hold both the request and the sender's profile
data class EnrichedFriendRequest(
    val friendshipId: String,
    val fromUser: User
)

class RequestsViewModel : ViewModel() {

    private val userRepository = UserRepository()

    sealed class RequestListState {
        object Loading : RequestListState()
        data class Success(val requests: List<EnrichedFriendRequest>) : RequestListState()
        data class Error(val message: String) : RequestListState()
    }

    private val _requestListState = MutableStateFlow<RequestListState>(RequestListState.Loading)
    val requestListState: StateFlow<RequestListState> = _requestListState

    init {
        fetchPendingRequests()
    }

    private fun fetchPendingRequests() {
        viewModelScope.launch {
            _requestListState.value = RequestListState.Loading
            val requestsResult = userRepository.getPendingFriendRequests()

            if (requestsResult.isSuccess) {
                val friendships = requestsResult.getOrNull() ?: emptyList()
                val enrichedRequests = mutableListOf<EnrichedFriendRequest>()

                // For each friendship, fetch the sender's user profile
                for (friendship in friendships) {
                    userRepository.getUserProfile(friendship.fromUserId).onSuccess { user ->
                        if (user != null) {
                            enrichedRequests.add(EnrichedFriendRequest(friendship.friendshipId, user))
                        }
                    }
                }
                _requestListState.value = RequestListState.Success(enrichedRequests)
            } else {
                _requestListState.value = RequestListState.Error(requestsResult.exceptionOrNull()?.message ?: "Failed to fetch requests.")
            }
        }
    }

    fun acceptRequest(friendshipId: String, fromUserId: String) {
        viewModelScope.launch {
            userRepository.acceptFriendRequest(friendshipId, fromUserId).onSuccess {
                fetchPendingRequests() // Refresh the list after accepting
            }
        }
    }

    fun declineRequest(friendshipId: String) {
        viewModelScope.launch {
            userRepository.declineFriendRequest(friendshipId).onSuccess {
                fetchPendingRequests() // Refresh the list after declining
            }
        }
    }
}
package com.yourname.campusconnect.matching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yourname.campusconnect.data.models.User
import com.yourname.campusconnect.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MatchingViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // --- USER LIST STATE ---
    sealed class UserListState {
        object Loading : UserListState()
        data class Success(val users: List<User>) : UserListState()
        data class Error(val message: String) : UserListState()
    }

    private val _userListState = MutableStateFlow<UserListState>(UserListState.Loading)
    val userListState: StateFlow<UserListState> = _userListState

    // --- FRIEND REQUEST STATE ---
    sealed class RequestState {
        object Idle : RequestState()
        object Sending : RequestState()
        object Sent : RequestState()
        data class Error(val message: String) : RequestState()
    }

    private val _requestState = MutableStateFlow<RequestState>(RequestState.Idle)
    val requestState: StateFlow<RequestState> = _requestState

    init {
        listenForUsers() // 🔄 start real-time listener immediately
    }

    // ✅ Real-time Firestore listener for all users except current and existing friends/pending
    private fun listenForUsers() {
        _userListState.value = UserListState.Loading
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _userListState.value =
                        UserListState.Error(error.message ?: "Failed to load users")
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    _userListState.value = UserListState.Success(emptyList())
                    return@addSnapshotListener
                }

                val allUsers = snapshot.toObjects(User::class.java)
                val filtered = allUsers.filter { it.uid != currentUserId }

                _userListState.value = UserListState.Success(filtered)
            }
    }

    // --- Send Friend Request ---
    fun sendFriendRequest(toUserId: String) {
        viewModelScope.launch {
            _requestState.value = RequestState.Sending
            val result = userRepository.sendFriendRequest(toUserId)
            if (result.isSuccess) {
                _requestState.value = RequestState.Sent
            } else {
                _requestState.value =
                    RequestState.Error(result.exceptionOrNull()?.message ?: "Failed to send request.")
            }
        }
    }

    fun resetRequestState() {
        _requestState.value = RequestState.Idle
    }
}

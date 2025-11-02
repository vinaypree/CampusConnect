package com.yourname.campusconnect.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yourname.campusconnect.data.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ChatMessage(
    val text: String,
    val isSentByMe: Boolean
)

class ChatViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _friends = MutableStateFlow<List<User>>(emptyList())
    val friends = _friends.asStateFlow()

    // 🟢 Load all accepted friends for the current user
    fun loadFriends() {
        viewModelScope.launch {
            try {
                val currentUserId = auth.currentUser?.uid ?: return@launch

                val snapshot = firestore.collection("friendships")
                    .whereEqualTo("status", "accepted")
                    .get()
                    .await()

                val friendships = snapshot.documents.mapNotNull { doc ->
                    val fromUserId = doc.getString("fromUserId")
                    val toUserId = doc.getString("toUserId")
                    if (fromUserId != null && toUserId != null &&
                        (fromUserId == currentUserId || toUserId == currentUserId)
                    ) {
                        if (fromUserId == currentUserId) toUserId else fromUserId
                    } else null
                }.distinct()

                if (friendships.isEmpty()) {
                    _friends.value = emptyList()
                    return@launch
                }

                val usersSnapshot = firestore.collection("users")
                    .whereIn("uid", friendships)
                    .get()
                    .await()

                val friendsList = usersSnapshot.toObjects(User::class.java)
                _friends.value = friendsList
            } catch (e: Exception) {
                e.printStackTrace()
                _friends.value = emptyList()
            }
        }
    }

    // 🟢 Simple send message (local state only)
    fun sendMessage(receiverId: String, text: String) {
        val newMessage = ChatMessage(text = text, isSentByMe = true)
        _messages.value = _messages.value + newMessage
    }
}












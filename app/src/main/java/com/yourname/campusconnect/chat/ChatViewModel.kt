package com.yourname.campusconnect.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnect.data.repository.ChatMessage
import com.campusconnect.data.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _unreadCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val unreadCounts: StateFlow<Map<String, Int>> = _unreadCounts

    private var unreadListener: ListenerRegistration? = null
    private var messagesListener: ListenerRegistration? = null

    init {
        listenForUnreadCounts()
    }

    /** 🔄 Real-time unread message listener (for badges) */
    private fun listenForUnreadCounts() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        unreadListener = repository.listenUnreadCounts(currentUserId) { counts ->
            _unreadCounts.value = counts
        }
    }

    /** 📥 Load all chat messages (real-time) */
    fun loadMessages(friendId: String) {
        val chatId = generateChatId(
            FirebaseAuth.getInstance().currentUser?.uid ?: "",
            friendId
        )

        messagesListener?.remove() // remove previous listener if any
        messagesListener = repository.listenForMessages(chatId, { messages ->
            _messages.value = messages.sortedBy { it.timestamp }
        }, { error ->
            error.printStackTrace()
        })
    }

    /** 🟢 Mark messages as read when chat is opened */
    fun markMessagesAsRead(friendId: String) {
        viewModelScope.launch {
            repository.markMessagesAsRead(friendId)
        }
    }

    /** ✉️ Send a new message */
    fun sendMessage(friendId: String, text: String) {
        viewModelScope.launch {
            repository.sendMessage(friendId, text)
        }
    }

    /** 🔑 Consistent chat ID for both participants */
    private fun generateChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_$uid2" else "${uid2}_$uid1"
    }

    /** 🧹 Clean up Firestore listeners */
    override fun onCleared() {
        unreadListener?.remove()
        messagesListener?.remove()
        super.onCleared()
    }
}

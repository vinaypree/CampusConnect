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

    private fun listenForUnreadCounts() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        unreadListener?.remove()
        unreadListener = repository.listenUnreadCounts(currentUserId) { counts ->
            _unreadCounts.value = counts
        }
    }

    fun loadMessages(friendId: String) {
        viewModelScope.launch {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val chatId = repository.getExistingChatId(currentUserId, friendId)

            messagesListener?.remove()
            messagesListener = repository.listenForMessages(
                chatId,
                onMessagesChanged = { messages ->
                    _messages.value = messages.sortedBy { it.timestamp }
                },
                onError = { it.printStackTrace() }
            )
        }
    }

    fun markMessagesAsRead(friendId: String) {
        viewModelScope.launch {
            repository.markMessagesAsRead(friendId)
        }
    }

    fun sendMessage(friendId: String, text: String) {
        viewModelScope.launch {
            repository.sendMessage(friendId, text)
        }
    }

    override fun onCleared() {
        unreadListener?.remove()
        messagesListener?.remove()
        super.onCleared()
    }
}
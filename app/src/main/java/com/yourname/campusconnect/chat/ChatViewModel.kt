package com.yourname.campusconnect.chat

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.yourname.campusconnect.data.models.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    fun loadMessages(currentUserId: String, friendId: String) {
        val chatId = getChatId(currentUserId, friendId)

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _messages.value = snapshot.toObjects(Message::class.java)
                }
            }
    }

    fun sendMessage(currentUserId: String, friendId: String, text: String) {
        val chatId = getChatId(currentUserId, friendId)
        val newMsg = Message(
            senderId = currentUserId,
            receiverId = friendId,
            message = text,
            timestamp = System.currentTimeMillis()
        )

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(newMsg)
    }

    private fun getChatId(user1: String, user2: String): String {
        return if (user1 < user2) "${user1}_${user2}" else "${user2}_${user1}"
    }
}

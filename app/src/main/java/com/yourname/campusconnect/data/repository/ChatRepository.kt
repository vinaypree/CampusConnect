package com.campusconnect.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

data class ChatMessage(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatSummary(
    val chatId: String = "",
    val participants: List<String> = listOf(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0L
)

class ChatRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val currentUserId get() = auth.currentUser?.uid

    private fun generateChatId(userId1: String, userId2: String): String {
        val sorted = listOf(userId1, userId2).sorted()
        return "${sorted[0]}_${sorted[1]}"
    }

    suspend fun sendMessage(receiverId: String, messageText: String) {
        val senderId = currentUserId ?: return
        val chatId = generateChatId(senderId, receiverId)
        val messageRef = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .document()

        val message = ChatMessage(
            messageId = messageRef.id,
            senderId = senderId,
            receiverId = receiverId,
            message = messageText,
            timestamp = System.currentTimeMillis()
        )

        firestore.runBatch { batch ->
            val chatDoc = firestore.collection("chats").document(chatId)
            batch.set(
                chatDoc,
                mapOf(
                    "chatId" to chatId,
                    "participants" to listOf(senderId, receiverId),
                    "lastMessage" to messageText,
                    "lastMessageTimestamp" to message.timestamp
                )
            )
            batch.set(messageRef, message)
        }.await()
    }

    fun listenForMessages(
        otherUserId: String,
        onMessagesChanged: (List<ChatMessage>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration? {
        val senderId = currentUserId ?: return null
        val chatId = generateChatId(senderId, otherUserId)

        return firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onError(e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.toObjects(ChatMessage::class.java)
                    onMessagesChanged(messages)
                }
            }
    }

    suspend fun getChatList(): List<ChatSummary> {
        val uid = currentUserId ?: return emptyList()
        val snapshot = firestore.collection("chats")
            .whereArrayContains("participants", uid)
            .get()
            .await()

        return snapshot.documents.mapNotNull { it.toObject(ChatSummary::class.java) }
    }
}

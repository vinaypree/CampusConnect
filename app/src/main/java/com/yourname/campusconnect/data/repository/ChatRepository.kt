package com.campusconnect.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val message: String? = null,
    val timestamp: Long = 0L,
    val unreadBy: List<String> = emptyList()
)

class ChatRepository {

    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    fun listenForMessages(
        chatId: String,
        onMessagesChanged: (List<ChatMessage>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    onError(e)
                    return@addSnapshotListener
                }

                val messages = snapshots?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                onMessagesChanged(messages)
            }
    }

    // 🔥 fixed: we emit chatId-based unread counts
    fun listenUnreadCounts(
        currentUserId: String,
        onCountsChanged: (Map<String, Int>) -> Unit
    ): ListenerRegistration {
        return db.collectionGroup("messages")
            .whereArrayContains("unreadBy", currentUserId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener

                val counts = mutableMapOf<String, Int>()
                snapshots?.documents?.forEach { doc ->
                    val chatId = doc.reference.parent.parent?.id ?: return@forEach
                    counts[chatId] = counts.getOrDefault(chatId, 0) + 1
                }
                onCountsChanged(counts)
            }
    }

    suspend fun sendMessage(friendId: String, text: String) {
        val senderId = currentUserId ?: return
        val chatId = generateChatId(senderId, friendId)
        val messageRef = db.collection("chats").document(chatId)
            .collection("messages").document()

        val message = hashMapOf(
            "senderId" to senderId,
            "text" to text,
            "timestamp" to System.currentTimeMillis(),
            "unreadBy" to listOf(friendId)
        )

        db.collection("chats").document(chatId).set(
            mapOf(
                "chatId" to chatId,
                "participants" to listOf(senderId, friendId),
                "lastMessage" to text,
                "lastMessageTimestamp" to System.currentTimeMillis()
            )
        )

        messageRef.set(message).await()
    }

    suspend fun markMessagesAsRead(friendId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val chatId = generateChatId(currentUserId, friendId)

        val messagesRef = db.collection("chats")
            .document(chatId)
            .collection("messages")

        val unread = messagesRef.whereArrayContains("unreadBy", currentUserId).get().await()
        for (doc in unread.documents) {
            messagesRef.document(doc.id)
                .update("unreadBy", FieldValue.arrayRemove(currentUserId))
        }
    }

    private fun generateChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_$uid2" else "${uid2}_$uid1"
    }
}

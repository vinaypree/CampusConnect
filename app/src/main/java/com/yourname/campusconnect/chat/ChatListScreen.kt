package com.yourname.campusconnect.chat

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun ChatListScreen(
    chatViewModel: ChatViewModel = viewModel(),
    onChatSelected: (String, String) -> Unit
) {
    val unreadCounts by chatViewModel.unreadCounts.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    var chatList by remember { mutableStateOf<List<ChatItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // 🔄 Load all chats and update live with unread badges
    LaunchedEffect(currentUserId, unreadCounts) {
        scope.launch {
            chatList = fetchAllFriendsWithChatData(currentUserId)
            isLoading = false
        }
    }

    when {
        isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }

        chatList.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No chats yet") }

        else -> LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            items(chatList.size) { i ->
                val chat = chatList[i]
                val unreadCount = unreadCounts[chat.chatId] ?: 0

                ChatListItem(
                    friendName = chat.friendName,
                    lastMessage = chat.lastMessage,
                    unreadCount = unreadCount,
                    onClick = { onChatSelected(chat.friendId, chat.friendName) }
                )
            }
        }
    }
}

@Composable
fun ChatListItem(
    friendName: String,
    lastMessage: String?,
    unreadCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(friendName, fontWeight = FontWeight.Bold)
                Text(
                    text = if (lastMessage.isNullOrBlank()) "Start chat" else lastMessage,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // 🔥 Unread badge
            if (unreadCount > 0) {
                Badge(containerColor = Color.Red) {
                    Text(
                        unreadCount.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

data class ChatItem(
    val chatId: String,
    val friendId: String,
    val friendName: String,
    val lastMessage: String?,
    val lastMessageTimestamp: Long = 0L
)

/** ✅ Fetch chats & friends while supporting both old and new chat IDs */
suspend fun fetchAllFriendsWithChatData(currentUserId: String): List<ChatItem> {
    val firestore = FirebaseFirestore.getInstance()
    val chatList = mutableListOf<ChatItem>()

    // Step 1: get all accepted friendships
    val friendshipsSnapshot = firestore.collection("friendships")
        .whereEqualTo("status", "accepted")
        .get()
        .await()

    val friendIds = friendshipsSnapshot.documents.mapNotNull { doc ->
        val fromId = doc.getString("fromUserId")
        val toId = doc.getString("toUserId")
        when (currentUserId) {
            fromId -> toId
            toId -> fromId
            else -> null
        }
    }.distinct()

    // Step 2: for each friend, fetch both old/new chat IDs
    for (friendId in friendIds) {
        val friendDoc = firestore.collection("users").document(friendId).get().await()
        val friendName = friendDoc.getString("name") ?: "Unknown User"

        val chatIdWithUnderscore =
            if (currentUserId < friendId) "${currentUserId}$friendId" else "${friendId}$currentUserId"
        val chatIdWithoutUnderscore =
            if (currentUserId < friendId) "${currentUserId}$friendId" else "${friendId}$currentUserId"

        // Try new format first, then old format
        val chatDoc = firestore.collection("chats").document(chatIdWithUnderscore).get().await()
        val finalChatDoc =
            if (!chatDoc.exists())
                firestore.collection("chats").document(chatIdWithoutUnderscore).get().await()
            else chatDoc

        val finalChatId =
            if (chatDoc.exists()) chatIdWithUnderscore else chatIdWithoutUnderscore
        val lastMessage = finalChatDoc.getString("lastMessage") ?: "Start chat"
        val timestamp = finalChatDoc.getLong("lastMessageTimestamp") ?: 0L

        chatList.add(ChatItem(finalChatId, friendId, friendName, lastMessage, timestamp))
    }

    // Step 3: sort by latest chat
    return chatList.sortedByDescending { it.lastMessageTimestamp }
}
package com.yourname.campusconnect.chat

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

    LaunchedEffect(currentUserId, unreadCounts) {
        scope.launch {
            chatList = fetchChatsForUser(currentUserId)
            isLoading = false
        }
    }

    when {
        isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        chatList.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No chats") }
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize().padding(8.dp)
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
fun ChatListItem(friendName: String, lastMessage: String?, unreadCount: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(friendName, fontWeight = FontWeight.Bold)
                if (!lastMessage.isNullOrEmpty()) {
                    Text(lastMessage, style = MaterialTheme.typography.bodySmall)
                }
            }

            if (unreadCount > 0) {
                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                    Text(unreadCount.toString())
                }
            }
        }
    }
}

data class ChatItem(
    val chatId: String,
    val friendId: String,
    val friendName: String,
    val lastMessage: String?
)

suspend fun fetchChatsForUser(currentUserId: String): List<ChatItem> {
    val firestore = FirebaseFirestore.getInstance()
    val chatList = mutableListOf<ChatItem>()
    val chatsSnapshot = firestore.collection("chats")
        .whereArrayContains("participants", currentUserId)
        .get()
        .await()

    for (doc in chatsSnapshot.documents) {
        val participants = doc.get("participants") as? List<String> ?: continue
        val friendId = participants.firstOrNull { it != currentUserId } ?: continue
        val friendName = firestore.collection("users").document(friendId)
            .get().await().getString("name") ?: "Unknown"
        chatList.add(
            ChatItem(
                chatId = doc.id,
                friendId = friendId,
                friendName = friendName,
                lastMessage = doc.getString("lastMessage")
            )
        )
    }

    return chatList.sortedByDescending { doc ->
        chatsSnapshot.documents.find { it.id == doc.chatId }
            ?.getLong("lastMessageTimestamp") ?: 0L
    }
}

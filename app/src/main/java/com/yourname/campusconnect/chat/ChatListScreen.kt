package com.yourname.campusconnect.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavController,
    currentUserId: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""
) {
    val firestore = FirebaseFirestore.getInstance()
    var friends by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            val fromFriends = firestore.collection("friendships")
                .whereEqualTo("fromUserId", currentUserId)
                .whereEqualTo("status", "accepted")
                .get()
                .await()

            val toFriends = firestore.collection("friendships")
                .whereEqualTo("toUserId", currentUserId)
                .whereEqualTo("status", "accepted")
                .get()
                .await()

            val friendIds = (fromFriends.documents.map { it.getString("toUserId") ?: "" } +
                    toFriends.documents.map { it.getString("fromUserId") ?: "" })
                .filter { it.isNotEmpty() }

            val friendData = mutableListOf<Pair<String, String>>()
            for (id in friendIds) {
                val userDoc = firestore.collection("users").document(id).get().await()
                val name = userDoc.getString("name") ?: "Unknown"
                friendData.add(id to name)
            }
            friends = friendData
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Chats") }) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(friends.size) { index ->
                val (friendId, friendName) = friends[index]
                ListItem(
                    headlineContent = { Text(friendName) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("chatDetail/$friendId/$friendName")
                        }
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                )
                Divider()
            }
        }
    }
}

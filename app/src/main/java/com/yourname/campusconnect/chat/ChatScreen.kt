package com.yourname.campusconnect.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    receiverId: String,
    receiverName: String = "Chat",
    currentUserId: String,
    chatViewModel: ChatViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    var message by remember { mutableStateOf(TextFieldValue("")) }
    val messages by chatViewModel.messages.collectAsState(initial = emptyList())
    val friends by chatViewModel.friends.collectAsState(initial = emptyList())

    // ✅ Load friends once
    LaunchedEffect(Unit) {
        chatViewModel.loadFriends()
    }

    // ✅ Check if receiver is an accepted friend
    val isFriend = friends.any { it.uid == receiverId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(receiverName) },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (isFriend) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        placeholder = { Text("Type a message...") }
                    )
                    IconButton(onClick = {
                        if (message.text.isNotBlank()) {
                            chatViewModel.sendMessage(receiverId, message.text)
                            message = TextFieldValue("")
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send"
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You can only chat with accepted friends.",
                        color = Color.Gray
                    )
                }
            }
        }
    ) { padding ->
        if (isFriend) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                reverseLayout = true
            ) {
                items(messages) { msg ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        contentAlignment = if (msg.isSentByMe) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Surface(
                            color = if (msg.isSentByMe)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                text = msg.text,
                                modifier = Modifier.padding(10.dp),
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "You’re not friends with this user yet.",
                    color = Color.Gray
                )
            }
        }
    }
}








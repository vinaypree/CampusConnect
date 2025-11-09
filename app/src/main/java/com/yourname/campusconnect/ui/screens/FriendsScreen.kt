package com.yourname.campusconnect.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yourname.campusconnect.data.models.User
import com.yourname.campusconnect.data.repository.UserRepository
import com.yourname.campusconnect.ui.theme.DarkBlueStart
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(navController: NavController) {
    val repository = remember { UserRepository() }
    var friends by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ Load accepted friends
    LaunchedEffect(Unit) {
        val result = repository.getAcceptedFriends()
        if (result.isSuccess) friends = result.getOrDefault(emptyList())
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Friends", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBlueStart)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }

                friends.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("No friends yet 😅")
                }

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(friends) { friend ->
                        FriendCard(
                            friend = friend,
                            navController = navController,
                            repository = repository,
                            snackbarHostState = snackbarHostState,
                            onUnfriend = {
                                friends = friends.filterNot { it.uid == friend.uid }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FriendCard(
    friend: User,
    navController: NavController,
    repository: UserRepository,
    snackbarHostState: SnackbarHostState,
    onUnfriend: () -> Unit
) {
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(friend.name ?: "Unknown", style = MaterialTheme.typography.titleMedium)
            Text(friend.email ?: "", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        navController.navigate("chatDetail/${friend.uid}/${friend.name}")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Message")
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val result = repository.unfriendUser(friend.uid ?: "")
                            if (result.isSuccess) {
                                snackbarHostState.showSnackbar("${friend.name} removed")
                                onUnfriend()
                            } else {
                                snackbarHostState.showSnackbar("Failed to remove ${friend.name}")
                            }
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Unfriend")
                }
            }
        }
    }
}

package com.yourname.campusconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import com.yourname.campusconnect.data.models.Post
import com.yourname.campusconnect.post.PostViewModel
import com.yourname.campusconnect.ui.theme.DarkBlueStart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    postViewModel: PostViewModel = viewModel(),
    onPostCreated: () -> Unit
) {
    var content by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf("Public") }
    val postState by postViewModel.postState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Create Post", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBlueStart),
                actions = {
                    Button(
                        onClick = {
                            if (content.isNotBlank()) {
                                val visibilityKey =
                                    if (visibility == "Friends Only") "friends" else "public"
                                val newPost = Post(
                                    content = content.trim(),
                                    visibility = visibilityKey,
                                    timestamp = Timestamp.now()
                                )
                                postViewModel.createPostObject(newPost)
                            }
                        },
                        enabled = postState !is PostViewModel.PostState.Loading
                    ) {
                        if (postState is PostViewModel.PostState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Post")
                        }
                    }
                }
            )
        },
        containerColor = DarkBlueStart
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            TextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("What's on your mind?", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Visibility:", color = Color.White, modifier = Modifier.padding(end = 8.dp))
                Button(
                    onClick = {
                        visibility = if (visibility == "Public") "Friends Only" else "Public"
                    },
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(Icons.Default.Public, contentDescription = "Visibility", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(visibility, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    LaunchedEffect(postState) {
        when (val state = postState) {
            is PostViewModel.PostState.Success -> {
                postViewModel.resetState()
                onPostCreated()
            }
            is PostViewModel.PostState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                postViewModel.resetState()
            }
            else -> {}
        }
    }
}

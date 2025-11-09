package com.yourname.campusconnect.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.yourname.campusconnect.data.models.User
import com.yourname.campusconnect.matching.MatchingViewModel
import com.yourname.campusconnect.ui.theme.BlueGradientStart
import com.yourname.campusconnect.ui.theme.DarkBlueStart
import com.yourname.campusconnect.ui.theme.LighterBlueEnd
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanionScreen(
    navController: NavController? = null,
    matchingViewModel: MatchingViewModel = viewModel()
) {
    val userListState by matchingViewModel.userListState.collectAsState()
    val requestState by matchingViewModel.requestState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                modifier = Modifier
                    .background(DarkBlueStart)
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            ) {
                // ✅ Removed extra top space
                TopAppBar(
                    title = { Text("Find a Friend", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBlueStart)
                )

                // 🔍 Search Bar
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White
                            )
                        },
                        placeholder = { /* removed text */ },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    ) { paddingValues ->
        when (val state = userListState) {
            is MatchingViewModel.UserListState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }

            is MatchingViewModel.UserListState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = Color.Red)
                }
            }

            is MatchingViewModel.UserListState.Success -> {
                val filteredUsers = state.users.filter {
                    it.name.contains(searchQuery.text, ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredUsers) { user ->
                        CompanionCard(
                            user = user,
                            onConnectClicked = {
                                matchingViewModel.sendFriendRequest(user.uid)
                            }
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(requestState) {
        when (val state = requestState) {
            is MatchingViewModel.RequestState.Sent -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Friend request sent!")
                }
                matchingViewModel.resetRequestState()
            }

            is MatchingViewModel.RequestState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(state.message)
                }
                matchingViewModel.resetRequestState()
            }

            else -> {}
        }
    }
}

@Composable
fun CompanionCard(
    user: User,
    onConnectClicked: () -> Unit
) {
    var requestSent by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LighterBlueEnd),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(56.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = user.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                    Text(text = "${user.department}, ${user.year} Year", fontSize = 12.sp, color = Color.LightGray)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Interests: ${user.interests.joinToString(", ")}", fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))
            Spacer(modifier = Modifier.height(12.dp))

            // ✅ Button right-aligned cleanly
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                    onClick = {
                        onConnectClicked()
                        requestSent = true
                    },
                    enabled = !requestSent,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (requestSent) Color.Gray else BlueGradientStart
                    )
                ) {
                    Text(if (requestSent) "Request Sent" else "Connect")
                }
            }
        }
    }
}

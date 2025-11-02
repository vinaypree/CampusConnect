package com.yourname.campusconnect.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourname.campusconnect.requests.EnrichedFriendRequest
import com.yourname.campusconnect.requests.RequestsViewModel
import com.yourname.campusconnect.ui.theme.DarkBlueStart
import com.yourname.campusconnect.ui.theme.LighterBlueEnd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsScreen(
    requestsViewModel: RequestsViewModel = viewModel()
) {
    val requestListState by requestsViewModel.requestListState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friend Requests", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBlueStart)
            )
        }
    ) { paddingValues ->
        when (val state = requestListState) {
            is RequestsViewModel.RequestListState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            is RequestsViewModel.RequestListState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(state.message, color = Color.Red)
                }
            }
            is RequestsViewModel.RequestListState.Success -> {
                if (state.requests.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Text("No new requests", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.requests) { enrichedRequest ->
                            RequestCard(
                                request = enrichedRequest,
                                onAccept = {
                                    requestsViewModel.acceptRequest(enrichedRequest.friendshipId, enrichedRequest.fromUser.uid)
                                },
                                onDecline = {
                                    requestsViewModel.declineRequest(enrichedRequest.friendshipId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RequestCard(
    request: EnrichedFriendRequest,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LighterBlueEnd),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Picture",
                modifier = Modifier.size(48.dp).clip(CircleShape),
                contentScale = ContentScale.Crop,
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = request.fromUser.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                Text(text = "${request.fromUser.department}, ${request.fromUser.year} Year", fontSize = 12.sp, color = Color.LightGray)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row {
                Button(onClick = onAccept, contentPadding = PaddingValues(horizontal = 16.dp)) {
                    Text("Accept")
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = onDecline, contentPadding = PaddingValues(horizontal = 16.dp)) {
                    Text("Decline")
                }
            }
        }
    }
}
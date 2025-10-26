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
import com.yourname.campusconnect.data.models.User // Import the real User model
import com.yourname.campusconnect.matching.MatchingViewModel
import com.yourname.campusconnect.ui.theme.BlueGradientStart
import com.yourname.campusconnect.ui.theme.DarkBlueStart
import com.yourname.campusconnect.ui.theme.LighterBlueEnd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanionScreen(
    // We now get the same ViewModel as the SkillSwapScreen
    matchingViewModel: MatchingViewModel = viewModel()
) {
    val userListState by matchingViewModel.userListState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find a Companion", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBlueStart)
            )
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // We use the real user list from the state
                    items(state.users) { user ->
                        CompanionCard(user = user)
                    }
                }
            }
        }
    }
}

@Composable
fun CompanionCard(user: User) { // The card now accepts a real User object
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
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
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

            // Interests Section, using the real interests list
            Text("Interests: ${user.interests.joinToString(", ")}", fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* TODO: Handle connect logic */ },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = BlueGradientStart)
            ) {
                Text("Connect")
            }
        }
    }
}
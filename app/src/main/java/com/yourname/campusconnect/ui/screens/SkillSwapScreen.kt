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
import com.yourname.campusconnect.data.models.User
import com.yourname.campusconnect.matching.MatchingViewModel
import com.yourname.campusconnect.ui.theme.BlueGradientStart
import com.yourname.campusconnect.ui.theme.LighterBlueEnd

@Composable
fun SkillSwapScreen(
    matchingViewModel: MatchingViewModel = viewModel()
) {
    val userListState by matchingViewModel.userListState.collectAsState()

    // The Scaffold and TopAppBar have been removed.
    // The screen content now depends on the state from the ViewModel.
    when (val state = userListState) {
        is MatchingViewModel.UserListState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        }
        is MatchingViewModel.UserListState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message, color = Color.Red)
            }
        }
        is MatchingViewModel.UserListState.Success -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.users) { user ->
                    SkillSwapCard(user = user)
                }
            }
        }
    }
}

@Composable
fun SkillSwapCard(user: User) {
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

            Text("Teaches: ${user.skillsCanTeach.joinToString(", ")}", fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))
            Spacer(modifier = Modifier.height(4.dp))
            Text("Wants to Learn: ${user.skillsWantToLearn.joinToString(", ")}", fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))

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
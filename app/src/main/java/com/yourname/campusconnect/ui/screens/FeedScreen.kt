package com.yourname.campusconnect.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Add these color definitions at the top of your FeedScreen.kt file

private val LighterBlueEnd = Color(0xFF173A5E)
private val BlueGradientStart = Color(0xFF4A90E2)

@Composable
fun FeedScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Placeholder Post Cards
        item {
            PostCard(
                authorName = "Ananya Singh",
                authorInfo = "B-Tech EE, 2nd Year",
                postText = "Excited for our AI final project!",
                isFriendsOnly = false // This will now show "Public"
            )
        }
        item {
            PostCard(
                authorName = "Arjun Reddy",
                authorInfo = "B-Tech CSE, 2nd Year",
                postText = "Anyone up for a game of football this evening?",
                isFriendsOnly = true // This will show "Friends Only"
            )
        }
    }
}

@Composable
fun PostCard(authorName: String, authorInfo: String, postText: String, isFriendsOnly: Boolean) {
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
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = authorName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    Text(text = authorInfo, fontSize = 12.sp, color = Color.LightGray)
                }
                Spacer(modifier = Modifier.weight(1f))

                // --- THIS IS THE FIX ---
                // Now shows a tag for both "Public" and "Friends Only"
                val visibilityText = if (isFriendsOnly) "Friends Only" else "Public"
                Text(visibilityText, fontSize = 10.sp, color = Color.LightGray)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = postText, fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                // --- SMALLER BUTTONS ---
                Button(
                    onClick = { /* Handle Like */ },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueGradientStart.copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp) // Smaller padding
                ) {
                    Text("Like", fontSize = 12.sp) // Smaller font size
                }
                Button(
                    onClick = { /* Handle Comment */ },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueGradientStart.copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp) // Smaller padding
                ) {
                    Text("Comment", fontSize = 12.sp) // Smaller font size
                }
            }
        }
    }
}
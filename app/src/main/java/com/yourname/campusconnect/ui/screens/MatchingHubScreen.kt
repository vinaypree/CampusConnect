package com.yourname.campusconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourname.campusconnect.ui.theme.DarkBlueStart
import com.yourname.campusconnect.ui.theme.LighterBlueEnd
import com.yourname.campusconnect.ui.theme.GreenGradientEnd // Assuming you have this color or similar
import com.yourname.campusconnect.ui.theme.BlueGradientStart // Assuming you have this color or similar


@Composable
fun MatchingHubScreen(
    onNavigateToSkillSwap: () -> Unit,
    onNavigateToCompanion: () -> Unit
) {
    val backgroundGradient = remember {
        Brush.verticalGradient(
            colors = listOf(DarkBlueStart, LighterBlueEnd)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundGradient) // Apply background gradient
            .padding(24.dp), // Increased padding
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "What would you like to do?", // More engaging text
            fontSize = 28.sp, // Slightly larger
            fontWeight = FontWeight.ExtraBold, // Bolder
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 48.dp) // More space below title
        )

        // Skill Swap Card
        ExpandedMatchingOptionCard(
            title = "Find a Skill Swap",
            description = "Teach a skill, learn a new one",
            icon = Icons.Default.SwapHoriz,
            onClick = onNavigateToSkillSwap,
            backgroundColor = GreenGradientEnd // Use a distinct color for card
        )

        Spacer(modifier = Modifier.height(24.dp)) // Increased space between cards

        // Companion Finder Card
        ExpandedMatchingOptionCard(
            title = "Find a Companion",
            description = "Find a study partner, gym buddy, or friend",
            icon = Icons.Default.Group,
            onClick = onNavigateToCompanion,
            backgroundColor = BlueGradientStart // Use another distinct color
        )
    }
}

@Composable
fun ExpandedMatchingOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    backgroundColor: Color // New parameter for card background
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp) // Make cards significantly taller
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp), // Slightly more rounded corners
        colors = CardDefaults.cardColors(containerColor = backgroundColor), // Use the passed background color
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp) // More pronounced shadow
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(56.dp), // Larger icon
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp)) // Space between icon and text
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp, // Larger title text
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = description,
                fontSize = 16.sp, // Larger description text
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}
package com.yourname.campusconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.yourname.campusconnect.ui.theme.BlueGradientStart
import com.yourname.campusconnect.ui.theme.DarkBlueStart
import com.yourname.campusconnect.ui.theme.GreenGradientEnd
import com.yourname.campusconnect.ui.theme.LighterBlueEnd

@Composable
fun MatchingHubScreen(
    onNavigateToSkillSwap: () -> Unit,
    onNavigateToCompanion: () -> Unit,
    onNavigateToFriends: () -> Unit // <-- This was the missing part
) {
    val backgroundGradient = remember {
        Brush.verticalGradient(colors = listOf(DarkBlueStart, LighterBlueEnd))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "What would you like to do?",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        ExpandedMatchingOptionCard(
            title = "Find a Skill Swap",
            description = "Teach a skill, learn a new one",
            icon = Icons.Default.SwapHoriz,
            onClick = onNavigateToSkillSwap,
            backgroundColor = GreenGradientEnd
        )

        Spacer(modifier = Modifier.height(24.dp))

        ExpandedMatchingOptionCard(
            title = "Find a Companion",
            description = "Find a study partner, gym buddy, or friend",
            icon = Icons.Default.Group,
            onClick = onNavigateToCompanion,
            backgroundColor = BlueGradientStart
        )

        Spacer(modifier = Modifier.height(24.dp))

        ExpandedMatchingOptionCard(
            title = "View My Friends",
            description = "See your accepted connections",
            icon = Icons.Default.People,
            onClick = onNavigateToFriends,
            backgroundColor = LighterBlueEnd
        )
    }
}

@Composable
fun ExpandedMatchingOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    backgroundColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
                modifier = Modifier.size(56.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White, textAlign = TextAlign.Center)
            Text(text = description, fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center)
        }
    }
}
package com.yourname.campusconnect.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// Define your colors (can be moved to Theme.kt later)
private val DarkBlueStart = Color(0xFF0A192F)
private val BlueGradientStart = Color(0xFF4A90E2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// It now accepts the main NavController for navigating to top-level screens like profile
fun MainScreen(mainNavController: NavController) {
    // This NavController is for the bottom bar screens ONLY
    val bottomNavController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CampusConnect", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBlueStart)
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = bottomNavController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Handle Add Post action */ },
                containerColor = BlueGradientStart
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Post", tint = Color.White)
            }
        }
    ) { innerPadding ->
        // This NavHost switches the content based on bottom bar clicks
        NavHost(
            navController = bottomNavController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                FeedScreen()
            }
//            composable("matches") {
//                // Placeholder for your Matches screen
//                PlaceholderScreen(text = "Matches Screen")
//            }
//            composable("chat") {
//                // Placeholder for your Chat screen
//                PlaceholderScreen(text = "Chat Screen")
//            }
            composable("profile") {
                // When "profile" is selected, we use the MAIN NavController
                // to navigate to the actual ProfileScreen in the parent navigator.
                // This is a common pattern for screens that might not be part of the bottom bar flow.
                mainNavController.navigate("profile")
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        NavigationItem("Home", "home", Icons.Default.Home),
        NavigationItem("Matches", "matches", Icons.Default.People),
        NavigationItem("Chat", "chat", Icons.Default.Chat),
        NavigationItem("Profile", "profile", Icons.Default.Person)
    )

    NavigationBar(containerColor = DarkBlueStart) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                // This now correctly highlights the selected item
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) { saveState = true }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.Gray,
                    selectedTextColor = Color.White,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}

// Data class for navigation items
data class NavigationItem(val title: String, val route: String, val icon: ImageVector)

// A simple placeholder screen for features we haven't built yet
//@Composable
//fun PlaceholderScreen(text: String) {
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        Text(text = text, color = Color.White)
//    }
//}
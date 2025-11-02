package com.yourname.campusconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yourname.campusconnect.chat.ChatListScreen
import com.yourname.campusconnect.ui.theme.BlueGradientStart
import com.yourname.campusconnect.ui.theme.DarkBlueStart
import com.yourname.campusconnect.ui.theme.LighterBlueEnd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(mainNavController: NavController) {
    val bottomNavController = rememberNavController()

    val gradientBrush = remember {
        Brush.verticalGradient(colors = listOf(DarkBlueStart, LighterBlueEnd))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CampusConnect", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBlueStart),
                actions = {
                    IconButton(onClick = { mainNavController.navigate("requests") }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Friend Requests",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                navController = bottomNavController,
                mainNavController = mainNavController
            )
        },
        floatingActionButton = {
            val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            if (currentRoute == "home") {
                FloatingActionButton(
                    onClick = { mainNavController.navigate("create_post") },
                    containerColor = BlueGradientStart
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Post", tint = Color.White)
                }
            }
        },
        modifier = Modifier.background(gradientBrush)
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                FeedScreen()
            }
            // 1. Change the route name here
            composable("explore") {
                MatchingHubScreen(
                    onNavigateToSkillSwap = { bottomNavController.navigate("skill_swap") },
                    onNavigateToCompanion = { bottomNavController.navigate("companion") },
                    onNavigateToFriends = { bottomNavController.navigate("friends") }
                )
            }
            composable("chat") {
                ChatListScreen(navController = mainNavController)
            }
            composable("skill_swap") {
                SkillSwapScreen(navController = mainNavController)
            }
            composable("companion") {
                CompanionScreen(navController = mainNavController)
            }
            composable("friends") {
                FriendsScreen(navController = mainNavController)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController, mainNavController: NavController) {
    val items = listOf(
        NavigationItem("Home", "home", Icons.Default.Home),
        // 2. Change the title and route here
        NavigationItem("Explore", "explore", Icons.Default.People),
        NavigationItem("Chat", "chat", Icons.AutoMirrored.Filled.Chat),
        NavigationItem("Profile", "profile", Icons.Default.Person)
    )

    NavigationBar(containerColor = DarkBlueStart) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    if (item.route == "profile") {
                        mainNavController.navigate(item.route)
                    } else {
                        navController.navigate(item.route) {
                            navController.graph.startDestinationRoute?.let { route ->
                                popUpTo(route) { saveState = true }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
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

data class NavigationItem(val title: String, val route: String, val icon: ImageVector)

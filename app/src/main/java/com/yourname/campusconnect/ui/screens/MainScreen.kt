package com.yourname.campusconnect.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yourname.campusconnect.chat.ChatListScreen
import com.yourname.campusconnect.chat.ChatViewModel
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
                            Icons.Default.Notifications,
                            contentDescription = "Requests",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(bottomNavController, mainNavController)
        },
        floatingActionButton = {
            val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            if (currentRoute == "home") {
                FloatingActionButton(
                    onClick = { mainNavController.navigate("create_post") },
                    containerColor = BlueGradientStart
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                }
            }
        },
        modifier = Modifier.background(gradientBrush)
    ) { padding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") { FeedScreen() }

            composable("matches") {
                MatchingHubScreen(
                    onNavigateToSkillSwap = { bottomNavController.navigate("skill_swap") },
                    onNavigateToCompanion = { bottomNavController.navigate("companion") },
                    onNavigateToFriends = { bottomNavController.navigate("friends") }
                )
            }

            composable("chat") {
                val chatViewModel = remember { ChatViewModel() }
                ChatListScreen(
                    chatViewModel = chatViewModel,
                    onChatSelected = { receiverId, receiverName ->
                        mainNavController.navigate("chatDetail/$receiverId/$receiverName")
                    }
                )
            }

            composable("skill_swap") { SkillSwapScreen(navController = mainNavController) }
            composable("companion") { CompanionScreen(navController = mainNavController) }
            composable("friends") { FriendsScreen(navController = mainNavController) }
        }
    }
}

@Composable
fun BottomNavigationBar(
    bottomNavController: NavController,
    mainNavController: NavController
) {
    val firestore = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var unreadCount by remember { mutableIntStateOf(0) }

    // 🔥 Real-time unread listener for all chats
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            firestore.collectionGroup("messages")
                .whereArrayContains("unreadBy", currentUserId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    unreadCount = snapshot?.size() ?: 0
                }
        }
    }

    val items = listOf(
        NavigationItem("Home", "home", Icons.Default.Home),
        NavigationItem("Matches", "matches", Icons.Default.People),
        NavigationItem("Chat", "chat", Icons.AutoMirrored.Filled.Chat),
        NavigationItem("Profile", "profile", Icons.Default.Person)
    )

    NavigationBar(containerColor = DarkBlueStart) {
        val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    if (item.route == "chat" && unreadCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = Color.Red,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        ) {
                            Icon(item.icon, contentDescription = item.title, tint = Color.White)
                        }
                    } else {
                        Icon(item.icon, contentDescription = item.title, tint = Color.White)
                    }
                },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    if (item.route == "profile") {
                        mainNavController.navigate(item.route)
                    } else {
                        bottomNavController.navigate(item.route) {
                            bottomNavController.graph.startDestinationRoute?.let { route ->
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

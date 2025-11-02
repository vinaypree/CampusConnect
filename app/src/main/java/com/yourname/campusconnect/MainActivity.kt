package com.yourname.campusconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.yourname.campusconnect.ui.screens.*
import com.yourname.campusconnect.ui.theme.CampusConnectTheme
import com.yourname.campusconnect.chat.ChatListScreen
import com.yourname.campusconnect.chat.ChatScreen
import com.yourname.campusconnect.chat.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CampusConnectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val chatViewModel: ChatViewModel = viewModel()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    NavHost(navController = navController, startDestination = "splash") {

        // Splash Screen
        composable("splash") { SplashScreen(navController = navController) }

        // Login Screen
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToSignUp = { navController.navigate("signup") },
                onForgotPassword = {}
            )
        }

        // Signup Screen
        composable("signup") {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate("profile") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // Profile Screen
        composable("profile") {
            ProfileScreen(
                onProfileSaved = {
                    navController.navigate("dashboard") {
                        popUpTo("profile") { inclusive = true }
                    }
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Dashboard Screen
        composable("dashboard") {
            MainScreen(mainNavController = navController)
        }

        // Create Post Screen
        composable("create_post") {
            CreatePostScreen(onPostCreated = { navController.popBackStack() })
        }

        // Friend Requests Screen
        composable("requests") {
            RequestsScreen()
        }

        // ✅ Chat List Screen — uses navController instead of callback
        composable("chat") {
            ChatListScreen(
                navController = navController,
                currentUserId = currentUserId
            )
        }

        // ✅ Chat Detail Screen — expects receiverId and receiverName
        composable("chatDetail/{receiverId}/{receiverName}") { backStackEntry ->
            val receiverId = backStackEntry.arguments?.getString("receiverId") ?: ""
            val receiverName = backStackEntry.arguments?.getString("receiverName") ?: "Chat"

            ChatScreen(
                navController = navController,
                currentUserId = currentUserId,
                receiverId = receiverId,
                receiverName = receiverName,
                chatViewModel = chatViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

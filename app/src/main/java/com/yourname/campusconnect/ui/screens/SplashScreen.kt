package com.yourname.campusconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.yourname.campusconnect.auth.SplashViewModel
import com.yourname.campusconnect.ui.theme.BlueGradientStart
import com.yourname.campusconnect.ui.theme.GreenGradientEnd



@Composable
fun SplashScreen(
    navController: NavController,
    splashViewModel: SplashViewModel = viewModel()
) {
    val userStatus by splashViewModel.userStatus.collectAsState()

    LaunchedEffect(userStatus) {
        when (userStatus) {
            SplashViewModel.UserStatus.LoggedOut -> {
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            SplashViewModel.UserStatus.NeedsProfile -> {
                navController.navigate("profile") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            SplashViewModel.UserStatus.LoggedIn -> {
                navController.navigate("dashboard") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            SplashViewModel.UserStatus.Loading -> {
                // Do nothing, just wait for the check to complete
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(BlueGradientStart, GreenGradientEnd))),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}




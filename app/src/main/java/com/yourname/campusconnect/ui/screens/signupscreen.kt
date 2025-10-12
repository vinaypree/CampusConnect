package com.yourname.campusconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourname.campusconnect.auth.AuthViewModel
import com.yourname.campusconnect.ui.theme.BlueGradientStart
import com.yourname.campusconnect.ui.theme.GreenGradientEnd
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    authViewModel: AuthViewModel = viewModel(),
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val authState by authViewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val gradientBrush = remember {
        Brush.verticalGradient(colors = listOf(BlueGradientStart, GreenGradientEnd))
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = gradientBrush)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "CampusConnect Logo",
                    modifier = Modifier.size(80.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "CREATE ACCOUNT",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Join your campus community",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(32.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Campus Email") },
                        leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (password != confirmPassword) {
                                scope.launch { snackbarHostState.showSnackbar("Passwords do not match.") }
                            } else {
                                authViewModel.signUp(email, password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        enabled = authState != AuthViewModel.AuthState.Loading,
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(colors = listOf(BlueGradientStart, GreenGradientEnd)),
                                    shape = RoundedCornerShape(25.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (authState is AuthViewModel.AuthState.Loading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Sign Up", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                TextButton(onClick = onNavigateToLogin) {
                    Row {
                        Text(text = "Already have an account? ", color = Color.White.copy(alpha = 0.8f))
                        Text(text = "Login", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }


    }


    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthViewModel.AuthState.Authenticated -> onSignUpSuccess()
            is AuthViewModel.AuthState.Error -> {
                snackbarHostState.showSnackbar(state.message, duration = SnackbarDuration.Long)
                authViewModel.resetState()
            }
            else -> {}
        }
    }


}
package com.yourname.campusconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout // ✅ FIX 1: Updated import
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourname.campusconnect.auth.AuthViewModel
import com.yourname.campusconnect.profile.ProfileViewModel
import com.yourname.campusconnect.ui.theme.BlueGradientStart
import com.yourname.campusconnect.ui.theme.GreenGradientEnd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onProfileSaved: () -> Unit,
    onLogout: () -> Unit
) {
    val userProfile by profileViewModel.userProfile.collectAsState()
    val profileState by profileViewModel.profileState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var name by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var skillsToTeach by remember { mutableStateOf("") }
    var skillsToLearn by remember { mutableStateOf("") }
    var interests by remember { mutableStateOf("") }

    LaunchedEffect(userProfile) {
        userProfile?.let {
            name = it.name
            department = it.department
            year = if (it.year > 0) it.year.toString() else ""
            bio = it.bio
            phone = it.phone
            skillsToTeach = it.skillsCanTeach.joinToString(", ")
            skillsToLearn = it.skillsWantToLearn.joinToString(", ")
            interests = it.interests.joinToString(", ")
        }
    }

    val gradientBrush = remember {
        Brush.verticalGradient(listOf(BlueGradientStart, GreenGradientEnd))
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        modifier = Modifier.background(gradientBrush),
        topBar = {
            TopAppBar(
                title = { Text("Your Profile", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    IconButton(onClick = { authViewModel.logout() }) {
                        Icon(
                            // ✅ FIX 2: Changed to the AutoMirrored version
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { /* No image picker yet */ },
                shape = CircleShape,
                modifier = Modifier.size(100.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = "Add Profile Picture",
                    modifier = Modifier.size(40.dp)
                )
            }
            Text(
                "Add Photo",
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            val textFieldColors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White,
                focusedIndicatorColor = Color.White,
                unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f),
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = department,
                onValueChange = { department = it },
                label = { Text("Department") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = year,
                onValueChange = { year = it },
                label = { Text("Year of Study") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Short Bio") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = skillsToTeach,
                onValueChange = { skillsToTeach = it },
                label = { Text("Skills you can teach (comma-separated)") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = skillsToLearn,
                onValueChange = { skillsToLearn = it },
                label = { Text("Skills you want to learn (comma-separated)") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = interests,
                onValueChange = { interests = it },
                label = { Text("Your Interests (comma-separated)") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val yearInt = year.toIntOrNull() ?: 0
                    val skillsTeachList = skillsToTeach.split(",").map { it.trim() }
                        .filter { it.isNotEmpty() }
                    val skillsLearnList = skillsToLearn.split(",").map { it.trim() }
                        .filter { it.isNotEmpty() }
                    val interestsList = interests.split(",").map { it.trim() }
                        .filter { it.isNotEmpty() }

                    profileViewModel.saveProfile(
                        name = name,
                        department = department,
                        year = yearInt,
                        bio = bio,
                        phone = phone,
                        skillsToTeach = skillsTeachList,
                        skillsToLearn = skillsLearnList,
                        interests = interestsList
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                enabled = profileState !is ProfileViewModel.ProfileState.Loading,
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(BlueGradientStart, GreenGradientEnd)
                            ),
                            shape = RoundedCornerShape(25.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (profileState is ProfileViewModel.ProfileState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            "Save Profile",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(profileState) {
        when (val state = profileState) {
            is ProfileViewModel.ProfileState.Success -> {
                snackbarHostState.showSnackbar("Profile Saved!", duration = SnackbarDuration.Short)
                onProfileSaved()
            }

            is ProfileViewModel.ProfileState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }

            else -> Unit
        }
    }

    // ✅ Logout detection only after explicit logout
    LaunchedEffect(authState) {
        val currentState = authState
        if (currentState is AuthViewModel.AuthState.Message &&
            currentState.message == "Logged out"
        ) {
            onLogout()
        }
    }

}




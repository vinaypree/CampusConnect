package com.yourname.campusconnect.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.yourname.campusconnect.data.models.Post
import com.yourname.campusconnect.data.repository.PostRepository
import com.yourname.campusconnect.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PostViewModel : ViewModel() {

    private val postRepository = PostRepository()
    private val userRepository = UserRepository()

    // Represents the state of the post creation process
    sealed class PostState {
        object Idle : PostState()
        object Loading : PostState()
        object Success : PostState()
        data class Error(val message: String) : PostState()
    }

    private val _postState = MutableStateFlow<PostState>(PostState.Idle)
    val postState: StateFlow<PostState> = _postState

    // in post/PostViewModel.kt

    fun createPost(content: String, visibility: String) {
        viewModelScope.launch {
            _postState.value = PostState.Loading

            if (content.isBlank()) {
                _postState.value = PostState.Error("Post content cannot be empty.")
                return@launch
            }

            // --- THIS IS THE CORRECTED PART ---

            // 1. Get the current user's ID
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                _postState.value = PostState.Error("You must be logged in to post.")
                return@launch
            }

            // 2. Safely fetch the user's profile
            val userResult = userRepository.getUserProfile(uid)
            val user = userResult.getOrNull()

            if (userResult.isFailure || user == null) {
                _postState.value = PostState.Error("Could not fetch user profile to create post.")
                return@launch
            }

            // 3. Create the Post object (user is now guaranteed to be non-null)
            val newPost = Post(
                authorId = user.uid,
                authorName = user.name,
                authorDepartment = user.department,
                authorYear = user.year,
                content = content,
                visibility = visibility,
                timestamp = Timestamp.now()
            )

            val result = postRepository.createPost(newPost)
            if (result.isSuccess) {
                _postState.value = PostState.Success
            } else {
                _postState.value = PostState.Error(result.exceptionOrNull()?.message ?: "Failed to create post.")
            }
        }
    }
}
package com.yourname.campusconnect.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.yourname.campusconnect.data.models.Post
import com.yourname.campusconnect.data.repository.PostRepository
import com.yourname.campusconnect.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PostViewModel : ViewModel() {

    private val postRepository = PostRepository()
    private val userRepository = UserRepository()

    sealed class PostState {
        object Idle : PostState()
        object Loading : PostState()
        object Success : PostState()
        data class Error(val message: String) : PostState()
    }

    private val _postState = MutableStateFlow<PostState>(PostState.Idle)
    val postState: StateFlow<PostState> = _postState

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private var postListener: ListenerRegistration? = null

    init {
        listenToPosts()
    }

    // 🔹 Firestore live listener — keeps comment counts fresh
    private fun listenToPosts() {
        postListener?.remove()
        postListener = postRepository.listenToPosts(
            onPostsChanged = { posts ->
                _posts.value = posts
            },
            onError = { e ->
                _postState.value = PostState.Error(e.message ?: "Failed to fetch posts")
            }
        )
    }

    // 🔹 Create post
    fun createPost(content: String, visibility: String) {
        viewModelScope.launch {
            _postState.value = PostState.Loading
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val user = userRepository.getUserProfile(uid).getOrNull() ?: return@launch

            if (content.isBlank()) {
                _postState.value = PostState.Error("Post cannot be empty")
                return@launch
            }

            val newPost = Post(
                authorId = uid,
                authorName = user.name,
                authorDepartment = user.department,
                authorYear = user.year,
                content = content,
                visibility = visibility,
                timestamp = Timestamp.now()
            )

            val result = postRepository.createPost(newPost)
            _postState.value = if (result.isSuccess) {
                PostState.Success
            } else {
                PostState.Error(result.exceptionOrNull()?.message ?: "Failed to post")
            }
        }
    }

    // 🔹 Add comment (increments count instantly in UI)
    fun addComment(post: Post, content: String) {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val user = userRepository.getUserProfile(uid).getOrNull() ?: return@launch

            val result = postRepository.addComment(post.postId, uid, user.name, content)

//            if (result.isSuccess) {
//                // 🔹 Instantly update comment count locally
//                val updatedPosts = _posts.value.map {
//                    if (it.postId == post.postId)
//                        it.copy(commentCount = it.commentCount + 1)
//                    else it
//                }
//                _posts.value = updatedPosts
//            }
        }
    }

    // 🔹 Toggle like (auto-updated by Firestore listener)
    fun likePost(post: Post) {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            postRepository.toggleLike(post.postId, uid)
        }
    }

    override fun onCleared() {
        super.onCleared()
        postListener?.remove()
    }
}

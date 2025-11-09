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

    private fun listenToPosts() {
        postListener?.remove()
        postListener = postRepository.listenToPosts(
            onPostsChanged = { posts ->
                val normalized = posts.map {
                    it.copy(visibility = when (it.visibility.lowercase()) {
                        "public" -> "public"
                        "friends", "friends only" -> "friends"
                        else -> "public"
                    })
                }
                _posts.value = normalized
            },
            onError = { e ->
                _postState.value = PostState.Error(e.message ?: "Failed to fetch posts")
            }
        )
    }

    fun createPostObject(post: Post) {
        viewModelScope.launch {
            _postState.value = PostState.Loading

            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val user = userRepository.getUserProfile(uid).getOrNull() ?: return@launch

            val visibility = when (post.visibility.lowercase()) {
                "friends only", "friends" -> "friends"
                else -> "public"
            }

            val finalPost = post.copy(
                authorId = uid,
                authorName = user.name,
                authorDepartment = user.department,
                authorYear = user.year,
                visibility = visibility,
                timestamp = Timestamp.now()
            )

            val result = postRepository.createPost(finalPost)
            _postState.value = if (result.isSuccess) PostState.Success
            else PostState.Error(result.exceptionOrNull()?.message ?: "Failed to post")
        }
    }

    fun resetState() {
        _postState.value = PostState.Idle
    }

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





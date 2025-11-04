package com.yourname.campusconnect.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import com.yourname.campusconnect.data.models.Post
import com.yourname.campusconnect.data.repository.PostRepository
import com.yourname.campusconnect.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

class FeedViewModel : ViewModel() {

    private val postRepository = PostRepository()
    private val userRepository = UserRepository()

    sealed class FeedState {
        object Loading : FeedState()
        data class Success(val posts: List<Post>) : FeedState()
        data class Error(val message: String) : FeedState()
    }

    private val _feedState = MutableStateFlow<FeedState>(FeedState.Loading)
    val feedState: StateFlow<FeedState> = _feedState

    private var listenerRegistration: ListenerRegistration? = null

    // Keep a cached set of friend IDs to filter Friends-Only posts
    private val friendIds = mutableSetOf<String>()
    private val currentUserId: String? get() = FirebaseAuth.getInstance().currentUser?.uid

    init {
        // Step 1: load friends first
        loadFriendIdsThenListen()
    }

    private fun loadFriendIdsThenListen() {
        viewModelScope.launch {
            _feedState.value = FeedState.Loading
            try {
                val result = userRepository.getAcceptedFriends()
                if (result.isSuccess) {
                    val friends = result.getOrNull() ?: emptyList()
                    friendIds.clear()
                    friendIds.addAll(friends.map { it.uid })
                } else {
                    friendIds.clear()
                }

                // then start listening to posts
                startListeningToPosts()
            } catch (e: Exception) {
                _feedState.value = FeedState.Error(e.message ?: "Failed to load friends")
                startListeningToPosts() // still try to listen to posts
            }
        }
    }

    private fun startListeningToPosts() {
        // remove previous listener if any
        listenerRegistration?.remove()

        listenerRegistration = postRepository.listenToPosts(
            onPostsChanged = { posts ->
                // Filter posts: Public OR Friends Only where author is friend or the current user
                val uid = currentUserId
                val filtered = posts.filter { post ->
                    when (post.visibility) {
                        "Public" -> true
                        "Friends Only" -> {
                            // show if author is current user or author is in friendIds
                            uid != null && (post.authorId == uid || friendIds.contains(post.authorId))
                        }
                        else -> true // default to public if unknown string
                    }
                }
                _feedState.value = FeedState.Success(filtered)
            },
            onError = { e ->
                _feedState.value = FeedState.Error(e.message ?: "Error loading feed")
            }
        )
    }

    fun fetchPosts() {
        viewModelScope.launch {
            _feedState.value = FeedState.Loading
            val result = postRepository.getAllPosts()
            if (result.isSuccess) {
                val posts = result.getOrNull() ?: emptyList()
                val uid = currentUserId
                val filtered = posts.filter { post ->
                    when (post.visibility) {
                        "Public" -> true
                        "Friends Only" -> uid != null && (post.authorId == uid || friendIds.contains(post.authorId))
                        else -> true
                    }
                }
                _feedState.value = FeedState.Success(filtered)
            } else {
                _feedState.value = FeedState.Error(result.exceptionOrNull()?.message ?: "Failed to fetch posts.")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }

    // --- PUBLIC ACTIONS: like and comment ---

    // Toggle like on a post for the current user
    fun likePost(post: Post) {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            postRepository.toggleLike(post.postId, uid)
            // no local state mutation required because listener will update feed
        }
    }

    // Add a comment: (we avoid UI changes; we accept content param)
    fun addComment(post: Post, content: String) {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            // fetch current user profile for name
            val userResult = userRepository.getUserProfile(uid)
            val user = userResult.getOrNull()
            val name = user?.name ?: "Unknown"
            postRepository.addComment(post.postId, uid, name, content)
            // listener will update commentCount when Firestore triggers
        }
    }
}

package com.yourname.campusconnect.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.yourname.campusconnect.data.models.Post
import com.yourname.campusconnect.data.repository.PostRepository
import com.yourname.campusconnect.data.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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

    private val friendIds = mutableSetOf<String>()
    private val currentUserId: String? get() = FirebaseAuth.getInstance().currentUser?.uid

    init {
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

                delay(300) // ensure friendIds ready
                startListeningToPosts()
            } catch (e: Exception) {
                _feedState.value = FeedState.Error(e.message ?: "Failed to load friends")
                startListeningToPosts()
            }
        }
    }

    private fun startListeningToPosts() {
        listenerRegistration?.remove()

        listenerRegistration = postRepository.listenToPosts(
            onPostsChanged = { posts ->
                val uid = currentUserId
                val normalized = posts.map {
                    it.copy(
                        visibility = when (it.visibility.lowercase()) {
                            "public" -> "public"
                            "friends", "friends only" -> "friends"
                            else -> "public"
                        }
                    )
                }

                val filtered = normalized.filter { post ->
                    when (post.visibility) {
                        "public" -> true
                        "friends" -> uid != null && (post.authorId == uid || friendIds.contains(post.authorId))
                        else -> true
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

                val normalized = posts.map {
                    it.copy(
                        visibility = when (it.visibility.lowercase()) {
                            "public" -> "public"
                            "friends", "friends only" -> "friends"
                            else -> "public"
                        }
                    )
                }

                val filtered = normalized.filter { post ->
                    when (post.visibility) {
                        "public" -> true
                        "friends" -> uid != null && (post.authorId == uid || friendIds.contains(post.authorId))
                        else -> true
                    }
                }

                _feedState.value = FeedState.Success(filtered)
            } else {
                _feedState.value =
                    FeedState.Error(result.exceptionOrNull()?.message ?: "Failed to fetch posts.")
            }
        }
    }

    fun likePost(post: Post) {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            postRepository.toggleLike(post.postId, uid)
        }
    }

    fun addComment(post: Post, content: String) {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            val userResult = userRepository.getUserProfile(uid)
            val user = userResult.getOrNull()
            val name = user?.name ?: "Unknown"
            postRepository.addComment(post.postId, uid, name, content)
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}

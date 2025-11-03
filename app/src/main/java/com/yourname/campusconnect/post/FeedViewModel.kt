package com.yourname.campusconnect.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import com.yourname.campusconnect.data.models.Post
import com.yourname.campusconnect.data.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {

    private val postRepository = PostRepository()

    sealed class FeedState {
        object Loading : FeedState()
        data class Success(val posts: List<Post>) : FeedState()
        data class Error(val message: String) : FeedState()
    }

    private val _feedState = MutableStateFlow<FeedState>(FeedState.Loading)
    val feedState: StateFlow<FeedState> = _feedState

    private var listenerRegistration: ListenerRegistration? = null

    init {
        startListeningToPosts()
    }

    private fun startListeningToPosts() {
        listenerRegistration = postRepository.listenToPosts(
            onPostsChanged = { posts ->
                _feedState.value = FeedState.Success(posts)
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
                _feedState.value = FeedState.Success(result.getOrNull() ?: emptyList())
            } else {
                _feedState.value = FeedState.Error(result.exceptionOrNull()?.message ?: "Failed to fetch posts.")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}

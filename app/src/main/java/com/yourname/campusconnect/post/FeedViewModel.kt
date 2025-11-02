package com.yourname.campusconnect.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.campusconnect.data.models.Post
import com.yourname.campusconnect.data.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {

    private val postRepository = PostRepository()

    // Represents the state of the feed
    sealed class FeedState {
        object Loading : FeedState()
        data class Success(val posts: List<Post>) : FeedState()
        data class Error(val message: String) : FeedState()
    }

    private val _feedState = MutableStateFlow<FeedState>(FeedState.Loading)
    val feedState: StateFlow<FeedState> = _feedState

    // When the ViewModel is created, it will automatically fetch the posts
    init {
        fetchPosts()
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
}






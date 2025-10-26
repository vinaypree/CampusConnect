package com.yourname.campusconnect.data.models

import com.google.firebase.Timestamp

data class Post(
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorDepartment: String = "", // e.g., "B-Tech CSE"
    val authorYear: Int = 0,
    val content: String = "", // The mandatory text/caption
    val imageUrl: String? = null, // The optional image URL
    val timestamp: Timestamp = Timestamp.now(),
    val visibility: String = "Public", // "Public" or "Friends Only"
    val likes: List<String> = emptyList(), // List of user IDs who liked
    val commentCount: Int = 0 // A counter for comments
)
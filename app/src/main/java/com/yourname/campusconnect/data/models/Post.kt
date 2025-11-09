package com.yourname.campusconnect.data.models

import com.google.firebase.Timestamp

data class Post(
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorDepartment: String = "", // e.g., "B.Tech CSE"
    val authorYear: Int = 0,
    val content: String = "", // Text or caption
    val imageUrl: String? = null, // Optional image URL
    val timestamp: Timestamp = Timestamp.now(),

    // 🔹 Visibility field - stored in Firestore as lowercase for filtering
    val visibility: String = "public", // either "public" or "friends"

    val likes: List<String> = emptyList(), // User IDs who liked
//    val commentCount: Int = 0 // Optional comment counter if needed
)

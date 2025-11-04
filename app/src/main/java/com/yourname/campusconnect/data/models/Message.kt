package com.yourname.campusconnect.data.models

data class Message(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "", // ✅ unified field name (matches Firestore & repository)
    val timestamp: Long = System.currentTimeMillis()
)

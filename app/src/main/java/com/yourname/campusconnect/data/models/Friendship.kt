package com.yourname.campusconnect.data.models

import com.google.firebase.Timestamp

data class Friendship(
    // A unique ID for the friendship document itself
    val friendshipId: String = "",
    // The user who sent the request
    val fromUserId: String = "",

    // The user who received the request
    val toUserId: String = "",
    // The current status of the request
    val status: String = "pending", // Can be "pending", "accepted", or "declined"
    // When the request was created
    val createdAt: Timestamp = Timestamp.now()

)
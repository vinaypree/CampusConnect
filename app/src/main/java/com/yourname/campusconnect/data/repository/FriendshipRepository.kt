package com.yourname.campusconnect.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.yourname.campusconnect.data.models.Friendship
import kotlinx.coroutines.tasks.await

class FriendshipRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val friendshipCollection = db.collection("friendships")

    // ✅ Send friend request
    suspend fun sendFriendRequest(toUserId: String): Boolean {
        val currentUser = auth.currentUser ?: return false

        // Construct friendship object
        val friendship = Friendship(
            fromUserId = currentUser.uid,
            toUserId = toUserId,
            status = "pending"
        )

        return try {
            // Write to Firestore (rules require fromUserId == currentUser.uid)
            friendshipCollection.add(friendship).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ✅ Accept friend request
    suspend fun acceptRequest(friendshipId: String): Boolean {
        val currentUser = auth.currentUser ?: return false

        return try {
            val friendshipRef = friendshipCollection.document(friendshipId)
            val doc = friendshipRef.get().await()
            if (doc.exists() && doc["toUserId"] == currentUser.uid) {
                friendshipRef.update("status", "accepted").await()
                true
            } else false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ✅ Decline friend request
    suspend fun declineRequest(friendshipId: String): Boolean {
        val currentUser = auth.currentUser ?: return false

        return try {
            val friendshipRef = friendshipCollection.document(friendshipId)
            val doc = friendshipRef.get().await()
            if (doc.exists() && doc["toUserId"] == currentUser.uid) {
                friendshipRef.update("status", "declined").await()
                true
            } else false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ✅ Fetch all friendships for current user (sent or received)
    suspend fun getAllFriendships(): QuerySnapshot? {
        val currentUser = auth.currentUser ?: return null
        return try {
            friendshipCollection
                .whereIn("status", listOf("pending", "accepted"))
                .whereArrayContainsAny("participants", listOf(currentUser.uid))
                .get()
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

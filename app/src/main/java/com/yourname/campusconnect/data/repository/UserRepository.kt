package com.yourname.campusconnect.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.yourname.campusconnect.data.models.Friendship
import com.yourname.campusconnect.data.models.User
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val friendshipsCollection = db.collection("friendships")
    private val auth = FirebaseAuth.getInstance()

    // ✅ Fetch all users except self, friends, and pending ones
    suspend fun getFilteredUsers(): Result<List<User>> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            // Fetch all users
            val allUsers = usersCollection.get().await().toObjects(User::class.java)

            // Fetch all friendships (sent and received)
            val snapshot = friendshipsCollection
                .whereIn("status", listOf("pending", "accepted"))
                .get()
                .await()
                .toObjects(Friendship::class.java)

            val connectedIds = snapshot.filter {
                it.fromUserId == currentUserId || it.toUserId == currentUserId
            }.flatMap {
                listOf(it.fromUserId, it.toUserId)
            }.distinct()

            val filteredUsers = allUsers.filter { it.uid != currentUserId && !connectedIds.contains(it.uid) }

            Result.success(filteredUsers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Create or Update User Profile ---
    suspend fun createUserProfile(user: User): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            usersCollection.document(uid).set(user.copy(uid = uid)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(uid: String): Result<User?> {
        return try {
            val doc = usersCollection.document(uid).get().await()
            Result.success(doc.toObject(User::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun hasUserProfile(): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return usersCollection.document(uid).get().await().exists()
    }

    // --- Send Friend Request ---
    suspend fun sendFriendRequest(toUserId: String): Result<Unit> {
        return try {
            val fromUserId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            // 🔍 Check if any request exists between these two users (both directions)
            val existing = friendshipsCollection
                .whereIn("status", listOf("pending", "accepted"))
                .get()
                .await()
                .toObjects(Friendship::class.java)
                .filter {
                    (it.fromUserId == fromUserId && it.toUserId == toUserId) ||
                            (it.fromUserId == toUserId && it.toUserId == fromUserId)
                }

            // ⚠️ If any existing request or friendship found
            if (existing.isNotEmpty()) {
                return Result.failure(Exception("Request already sent or already friends"))
            }

            // ✅ Otherwise, create new request
            val newDoc = friendshipsCollection.document()
            val friendship = Friendship(
                friendshipId = newDoc.id,
                fromUserId = fromUserId,
                toUserId = toUserId,
                status = "pending"
            )

            newDoc.set(friendship).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // --- Get Pending Friend Requests ---
    suspend fun getPendingFriendRequests(): Result<List<Friendship>> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            val snapshot = friendshipsCollection
                .whereEqualTo("toUserId", currentUserId)
                .whereEqualTo("status", "pending")
                .get()
                .await()
            Result.success(snapshot.toObjects(Friendship::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Accept Friend Request (FIXED FULLY) ---
    suspend fun acceptFriendRequest(friendshipId: String, fromUserId: String): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            val friendshipRef = friendshipsCollection.document(friendshipId)
            val currentUserRef = usersCollection.document(currentUserId)
            val fromUserRef = usersCollection.document(fromUserId)

            // ✅ Use batch to ensure atomic updates
            db.runBatch { batch ->
                batch.update(friendshipRef, "status", "accepted")
                batch.update(currentUserRef, "friends", FieldValue.arrayUnion(fromUserId))
                batch.update(fromUserRef, "friends", FieldValue.arrayUnion(currentUserId))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Decline Friend Request ---
    suspend fun declineFriendRequest(friendshipId: String): Result<Unit> {
        return try {
            friendshipsCollection.document(friendshipId)
                .update("status", "declined")
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Get Accepted Friends ---
    suspend fun getAcceptedFriends(): Result<List<User>> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            val snapshot = friendshipsCollection
                .whereEqualTo("status", "accepted")
                .get()
                .await()

            val friendships = snapshot.toObjects(Friendship::class.java)
                .filter { it.fromUserId == currentUserId || it.toUserId == currentUserId }

            val friendIds = friendships.map {
                if (it.fromUserId == currentUserId) it.toUserId else it.fromUserId
            }.distinct()

            if (friendIds.isEmpty()) return Result.success(emptyList())

            val friendsSnapshot = usersCollection.whereIn("uid", friendIds).get().await()
            val friends = friendsSnapshot.toObjects(User::class.java)

            Result.success(friends)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

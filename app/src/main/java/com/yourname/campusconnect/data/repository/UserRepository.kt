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
    private val chatsCollection = db.collection("chats")
    private val auth = FirebaseAuth.getInstance()

    // ✅ Fetch all users except self, friends, and pending ones
    suspend fun getFilteredUsers(): Result<List<User>> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            val allUsers = usersCollection.get().await().toObjects(User::class.java)

            val snapshot = friendshipsCollection
                .whereIn("status", listOf("pending", "accepted"))
                .get().await()
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

    suspend fun sendFriendRequest(toUserId: String): Result<Unit> {
        return try {
            val fromUserId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            val existing = friendshipsCollection
                .whereIn("status", listOf("pending", "accepted"))
                .get().await()
                .toObjects(Friendship::class.java)
                .filter {
                    (it.fromUserId == fromUserId && it.toUserId == toUserId) ||
                            (it.fromUserId == toUserId && it.toUserId == fromUserId)
                }

            if (existing.isNotEmpty()) {
                return Result.failure(Exception("Request already sent or already friends"))
            }

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

    suspend fun getPendingFriendRequests(): Result<List<Friendship>> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            val snapshot = friendshipsCollection
                .whereEqualTo("toUserId", currentUserId)
                .whereEqualTo("status", "pending")
                .get().await()
            Result.success(snapshot.toObjects(Friendship::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptFriendRequest(friendshipId: String, fromUserId: String): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            val friendshipRef = friendshipsCollection.document(friendshipId)
            val currentUserRef = usersCollection.document(currentUserId)
            val fromUserRef = usersCollection.document(fromUserId)

            db.runBatch { batch ->
                batch.update(friendshipRef, "status", "accepted")
                batch.update(currentUserRef, "friends", FieldValue.arrayUnion(fromUserId))
                batch.update(fromUserRef, "friends", FieldValue.arrayUnion(currentUserId))
            }.await()

            // ✅ Create placeholder chat doc
            createInitialChatDocument(currentUserId, fromUserId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    suspend fun getAcceptedFriends(): Result<List<User>> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            val snapshot = friendshipsCollection
                .whereEqualTo("status", "accepted")
                .get().await()

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

    suspend fun unfriendUser(friendId: String): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            val friendshipSnapshot = friendshipsCollection
                .whereEqualTo("status", "accepted")
                .get().await()

            val friendshipDoc = friendshipSnapshot.documents.firstOrNull { doc ->
                val fromUser = doc.getString("fromUserId")
                val toUser = doc.getString("toUserId")
                (fromUser == currentUserId && toUser == friendId) ||
                        (fromUser == friendId && toUser == currentUserId)
            }

            if (friendshipDoc != null) friendshipsCollection.document(friendshipDoc.id).delete().await()

            db.runBatch { batch ->
                batch.update(usersCollection.document(currentUserId), "friends", FieldValue.arrayRemove(friendId))
                batch.update(usersCollection.document(friendId), "friends", FieldValue.arrayRemove(currentUserId))
            }.await()

            val chatId = if (currentUserId < friendId) "${currentUserId}_${friendId}" else "${friendId}_${currentUserId}"
            val chatRef = chatsCollection.document(chatId)
            val messages = chatRef.collection("messages").get().await()
            for (msg in messages.documents) msg.reference.delete().await()
            chatRef.delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ FIXED — Adds "unreadBy" and placeholder text
    private suspend fun createInitialChatDocument(user1: String, user2: String) {
        val chatId = if (user1 < user2) "${user1}_$user2" else "${user2}_$user1"
        val chatDoc = chatsCollection.document(chatId)
        val snapshot = chatDoc.get().await()

        if (!snapshot.exists()) {
            chatDoc.set(
                mapOf(
                    "chatId" to chatId,
                    "participants" to listOf(user1, user2),
                    "lastMessage" to "Start chat",
                    "lastMessageTimestamp" to System.currentTimeMillis(),
                    "unreadBy" to listOf<String>()
                )
            ).await()
        }
    }
}

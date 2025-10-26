package com.yourname.campusconnect.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yourname.campusconnect.data.models.User
import kotlinx.coroutines.tasks.await
import kotlin.Result

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val auth = FirebaseAuth.getInstance()

    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val currentUserId = auth.currentUser?.uid
            val snapshot = usersCollection.get().await()
            val users = snapshot.toObjects(User::class.java)
            // Filter out the current user so they don't see themselves in the list
            val otherUsers = users.filter { it.uid != currentUserId }
            Result.success(otherUsers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createUserProfile(user: User): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid
            require(uid != null) { "User is not logged in." }
            usersCollection.document(uid).set(user.copy(uid = uid)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(uid: String): Result<User?> {
        return try {
            val document = usersCollection.document(uid).get().await()
            val user = document.toObject(User::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun hasUserProfile(): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            usersCollection.document(uid).get().await().exists()
        } catch (e: Exception) {
            false
        }
    }
}
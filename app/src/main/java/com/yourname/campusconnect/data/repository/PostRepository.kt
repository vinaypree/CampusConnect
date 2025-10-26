package com.yourname.campusconnect.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.yourname.campusconnect.data.models.Post
import kotlinx.coroutines.tasks.await

class PostRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val postsCollection = db.collection("posts")

    suspend fun createPost(post: Post): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid
            require(uid != null) { "User is not logged in." }

            val newPostRef = postsCollection.document()

            newPostRef.set(post.copy(postId = newPostRef.id, authorId = uid)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- NEW FUNCTION ---
    suspend fun getAllPosts(): Result<List<Post>> {
        return try {
            val snapshot = postsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING) // Show newest posts first
                .limit(20) // Get the latest 20 posts
                .get()
                .await()

            val posts = snapshot.toObjects(Post::class.java)
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
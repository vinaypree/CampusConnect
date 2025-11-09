package com.yourname.campusconnect.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.yourname.campusconnect.data.models.Post
import kotlinx.coroutines.tasks.await

class PostRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val postsCollection = db.collection("posts")

    // 🔹 Create new post
    suspend fun createPost(post: Post): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val newPostRef = postsCollection.document()

            val postWithDefaults = post.copy(
                postId = newPostRef.id,
                authorId = uid,
                likes = emptyList(),
                visibility = when (post.visibility.lowercase()) {
                    "friends", "friends only" -> "friends"
                    else -> "public"
                },
                timestamp = Timestamp.now()
            )

            newPostRef.set(postWithDefaults).await()
            Log.d("PostRepository", "✅ Post created with ID: ${newPostRef.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PostRepository", "❌ Failed to create post: ${e.message}", e)
            Result.failure(e)
        }
    }

    // 🔹 Get all posts (no filtering here)
    suspend fun getAllPosts(): Result<List<Post>> {
        return try {
            val snapshot = postsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            val posts = snapshot.toObjects(Post::class.java)
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Real-time listener (no nested queries)
    fun listenToPosts(
        onPostsChanged: (List<Post>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return postsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    onError(e)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    val posts = snapshots.toObjects(Post::class.java)
                    onPostsChanged(posts)
                }
            }
    }

    // 🔹 Like / Unlike
    suspend fun toggleLike(postId: String, userId: String): Result<Unit> {
        return try {
            val postRef = postsCollection.document(postId)
            val snapshot = postRef.get().await()
            val currentLikes = (snapshot.get("likes") as? List<String>) ?: emptyList()
            val liked = currentLikes.contains(userId)
            if (liked) postRef.update("likes", FieldValue.arrayRemove(userId)).await()
            else postRef.update("likes", FieldValue.arrayUnion(userId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Add comment
    suspend fun addComment(
        postId: String,
        commenterId: String,
        commenterName: String,
        content: String
    ): Result<Unit> {
        return try {
            val postRef = postsCollection.document(postId)
            val commentRef = postRef.collection("comments").document()
            val commentMap = mapOf(
                "commentId" to commentRef.id,
                "authorId" to commenterId,
                "authorName" to commenterName,
                "content" to content,
                "timestamp" to Timestamp.now()
            )
            val batch = db.batch()
            batch.set(commentRef, commentMap)
            batch.update(postRef, "commentCount", FieldValue.increment(1))
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
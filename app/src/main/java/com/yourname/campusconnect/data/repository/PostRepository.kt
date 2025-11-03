package com.yourname.campusconnect.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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

    // Existing one-time fetch method
    suspend fun getAllPosts(): Result<List<Post>> {
        return try {
            val snapshot = postsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()

            val posts = snapshot.toObjects(Post::class.java)
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔥 NEW REAL-TIME LISTENER for live feed updates
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
}

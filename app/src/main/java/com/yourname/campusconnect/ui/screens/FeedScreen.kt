package com.yourname.campusconnect.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yourname.campusconnect.data.models.Post
import com.yourname.campusconnect.post.FeedViewModel
import com.yourname.campusconnect.ui.theme.BlueGradientStart
import com.yourname.campusconnect.ui.theme.LighterBlueEnd
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FeedScreen(feedViewModel: FeedViewModel = viewModel()) {
    val feedState by feedViewModel.feedState.collectAsState()

    when (val state = feedState) {
        is FeedViewModel.FeedState.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(color = Color.White) }

        is FeedViewModel.FeedState.Error -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { Text(text = state.message, color = Color.Red) }

        is FeedViewModel.FeedState.Success -> {
            if (state.posts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No posts yet. Be the first to share!", color = Color.White)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.posts) { post ->
                        PostCard(post = post, onLikeClick = { feedViewModel.likePost(post) })
                    }
                }
            }
        }
    }
}

@Composable
fun PostCard(post: Post, onLikeClick: () -> Unit) {
    var showComments by remember { mutableStateOf(false) }
    var comments by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var commentText by remember { mutableStateOf(TextFieldValue("")) }

    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LighterBlueEnd),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(post.authorName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    Text("${post.authorDepartment}, ${post.authorYear} Year", fontSize = 12.sp, color = Color.LightGray)
                    Text(formatTimestampToIST(post.timestamp), fontSize = 11.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(post.visibility, fontSize = 10.sp, color = Color.LightGray)
            }

            Spacer(modifier = Modifier.height(12.dp))
            PostContentText(post.content)  // White clickable text
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onLikeClick,
                    colors = ButtonDefaults.buttonColors(containerColor = BlueGradientStart.copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) { Text("Like (${post.likes.size})", fontSize = 12.sp) }

                Button(
                    onClick = { showComments = !showComments },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueGradientStart.copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) { Text("Comment", fontSize = 12.sp) }
            }

            if (showComments) {
                Spacer(modifier = Modifier.height(12.dp))
                LaunchedEffect(post.postId) {
                    db.collection("posts").document(post.postId)
                        .collection("comments")
                        .orderBy("timestamp")
                        .addSnapshotListener { snapshot, _ ->
                            snapshot?.let { comments = it.documents.mapNotNull { doc -> doc.data } }
                        }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    comments.forEach { comment ->
                        val name = comment["commenterName"] as? String ?: "Anonymous"
                        val text = comment["content"] as? String ?: ""
                        Text("$name: $text", fontSize = 13.sp, color = Color.White.copy(alpha = 0.9f))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .padding(8.dp),
                        decorationBox = { inner ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (commentText.text.isEmpty())
                                    Text("Write a comment...", color = Color.Gray, fontSize = 13.sp)
                                inner()
                            }
                        }
                    )

                    Button(
                        onClick = {
                            val text = commentText.text.trim()
                            if (text.isNotEmpty()) {
                                val uid = currentUser?.uid ?: return@Button
                                val usersRef = db.collection("users").document(uid)
                                coroutineScope.launch {
                                    try {
                                        val snapshot = usersRef.get().await()
                                        val name = snapshot.getString("name") ?: "Anonymous"
                                        db.collection("posts").document(post.postId)
                                            .collection("comments")
                                            .add(
                                                mapOf(
                                                    "commenterId" to uid,
                                                    "commenterName" to name,
                                                    "content" to text,
                                                    "timestamp" to com.google.firebase.Timestamp.now()
                                                )
                                            )
                                        commentText = TextFieldValue("")
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueGradientStart)
                    ) { Text("Post", fontSize = 12.sp) }
                }
            }
        }
    }
}

@Composable
fun PostContentText(postText: String) {
    val context = LocalContext.current
    val annotatedText = buildAnnotatedString {
        val regex = "(https?://[\\w./?=&%-]+)".toRegex()
        var lastIndex = 0

        regex.findAll(postText).forEach { match ->
            val range = match.range

            append(postText.substring(lastIndex, range.first))

            pushStringAnnotation(tag = "URL", annotation = match.value)
            withStyle(
                style = SpanStyle(
                    color = Color(0xFF64B5F6),
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(match.value)
            }
            pop()

            lastIndex = range.last + 1
        }

        append(postText.substring(lastIndex))
    }

    SelectionContainer {
        ClickableText(
            text = annotatedText,
            style = TextStyle(color = Color.White),   // 🔥 MAKE POST TEXT WHITE
            onClick = { offset ->
                annotatedText.getStringAnnotations("URL", offset, offset)
                    .firstOrNull()?.let {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.item))
                        context.startActivity(intent)
                    }
            }
        )
    }
}

fun formatTimestampToIST(timestamp: com.google.firebase.Timestamp?): String {
    if (timestamp == null) return ""
    val date = timestamp.toDate()
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
    sdf.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
    return sdf.format(date)
}

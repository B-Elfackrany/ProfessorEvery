package com.professorevery.app.ui.post

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.professorevery.app.databinding.ActivityCommentsBinding
import com.professorevery.app.model.Comment
import com.professorevery.app.model.Post

class CommentsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCommentsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: CommentAdapter
    private lateinit var postId: String
    private var currentPost: Post? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postId = intent.getStringExtra("postId") ?: ""
        if (postId.isEmpty()) {
            finish()
            return
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        loadPost()
        loadComments()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "댓글"
        }
    }

    private fun setupRecyclerView() {
        adapter = CommentAdapter()
        binding.commentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CommentsActivity)
            adapter = this@CommentsActivity.adapter
        }
    }

    private fun setupClickListeners() {
        binding.sendButton.setOnClickListener {
            val commentText = binding.commentInput.text.toString().trim()
            if (commentText.isNotEmpty()) {
                addComment(commentText)
            }
        }

        binding.likeButton.setOnClickListener {
            currentPost?.let { post -> toggleLike(post) }
        }
    }

    private fun loadPost() {
        db.collection("posts").document(postId).get()
            .addOnSuccessListener { document ->
                val post = document.toObject(Post::class.java)?.copy(id = document.id)
                post?.let {
                    currentPost = it
                    updatePostUI(it)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "게시물을 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun updatePostUI(post: Post) {
        binding.apply {
            authorName.text = post.authorName
            postTitle.text = post.title
            postContent.text = post.content
            likeCount.text = post.likes.toString()
            commentCount.text = post.comments.toString()

            val currentUserId = auth.currentUser?.uid
            val isLiked = currentUserId != null && post.likedBy.contains(currentUserId)
            likeIcon.setImageResource(
                if (isLiked) android.R.drawable.btn_star_big_on
                else android.R.drawable.btn_star_big_off
            )
        }
    }

    private fun loadComments() {
        db.collection("comments")
            .whereEqualTo("postId", postId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                val comments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Comment::class.java)?.copy(id = doc.id)
                } ?: listOf()

                adapter.submitList(comments)
            }
    }

    private fun addComment(commentText: String) {
        val currentUser = auth.currentUser ?: return
        binding.sendButton.isEnabled = false

        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                val authorName = document.getString("name") ?: "Unknown"
                
                val comment = Comment(
                    postId = postId,
                    authorId = currentUser.uid,
                    authorName = authorName,
                    content = commentText,
                    timestamp = System.currentTimeMillis()
                )

                db.collection("comments").add(comment.toMap())
                    .addOnSuccessListener {
                        binding.commentInput.text?.clear()
                        binding.sendButton.isEnabled = true
                        
                        // Update comment count in post
                        currentPost?.let { post ->
                            db.collection("posts").document(postId)
                                .update("comments", post.comments + 1)
                        }
                    }
                    .addOnFailureListener { e ->
                        binding.sendButton.isEnabled = true
                        Toast.makeText(this, "댓글 작성에 실패했습니다: ${e.message}", 
                            Toast.LENGTH_SHORT).show()
                    }
            }
    }

    private fun toggleLike(post: Post) {
        val userId = auth.currentUser?.uid ?: return
        val postRef = db.collection("posts").document(post.id)

        if (post.likedBy.contains(userId)) {
            postRef.update(
                mapOf(
                    "likes" to (post.likes - 1),
                    "likedBy" to post.likedBy.filter { it != userId }
                )
            )
        } else {
            postRef.update(
                mapOf(
                    "likes" to (post.likes + 1),
                    "likedBy" to post.likedBy + userId
                )
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 
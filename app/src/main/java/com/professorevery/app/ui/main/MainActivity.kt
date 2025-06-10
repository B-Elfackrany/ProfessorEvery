package com.professorevery.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.professorevery.app.R
import com.professorevery.app.databinding.ActivityMainBinding
import com.professorevery.app.model.Post
import com.professorevery.app.ui.auth.LoginActivity
import com.professorevery.app.ui.post.CreatePostActivity
import com.professorevery.app.ui.post.CommentsActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupToolbar()
        setupRecyclerView()
        setupFab()
        loadPosts()

        binding.swipeRefresh.setOnRefreshListener {
            loadPosts()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }

    private fun setupRecyclerView() {
        adapter = PostAdapter(
            onLikeClick = { post -> toggleLike(post) },
            onCommentClick = { post -> openComments(post) }
        )
        binding.postsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun setupFab() {
        binding.fabCreatePost.setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
        }
    }

    private fun loadPosts() {
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                binding.swipeRefresh.isRefreshing = false
                
                if (e != null) {
                    return@addSnapshotListener
                }

                val posts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Post::class.java)?.copy(id = doc.id)
                } ?: listOf()

                adapter.submitList(posts)
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

    private fun openComments(post: Post) {
        val intent = Intent(this, CommentsActivity::class.java)
        intent.putExtra("postId", post.id)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
} 
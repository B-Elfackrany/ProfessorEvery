package com.professorevery.app.ui.post

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.professorevery.app.databinding.ActivityCreatePostBinding
import com.professorevery.app.model.Post

class CreatePostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreatePostBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupToolbar()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "새 글 작성"
        }
    }

    private fun setupClickListeners() {
        binding.submitButton.setOnClickListener {
            val title = binding.titleInput.text.toString()
            val content = binding.contentInput.text.toString()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "제목과 내용을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            createPost(title, content)
        }
    }

    private fun createPost(title: String, content: String) {
        val currentUser = auth.currentUser ?: return
        binding.submitButton.isEnabled = false

        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                val authorName = document.getString("name") ?: "Unknown"
                
                val post = Post(
                    authorId = currentUser.uid,
                    authorName = authorName,
                    title = title,
                    content = content,
                    timestamp = System.currentTimeMillis()
                )

                db.collection("posts").add(post.toMap())
                    .addOnSuccessListener {
                        finish()
                    }
                    .addOnFailureListener { e ->
                        binding.submitButton.isEnabled = true
                        Toast.makeText(this, "게시물 작성에 실패했습니다: ${e.message}", 
                            Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                binding.submitButton.isEnabled = true
                Toast.makeText(this, "사용자 정보를 가져오는데 실패했습니다: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 
package com.professorevery.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.professorevery.app.databinding.ActivitySignupBinding
import com.professorevery.app.ui.main.MainActivity

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.signupButton.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()
            val name = binding.nameInput.text.toString()
            val university = binding.universityInput.text.toString()

            if (email.isEmpty() || password.isEmpty() || name.isEmpty() || university.isEmpty()) {
                Toast.makeText(this, "모든 필드를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isEducationalEmail(email)) {
                Toast.makeText(this, "교육용 이메일만 사용 가능합니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "비밀번호는 6자 이상이어야 합니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            createUser(email, password, name, university)
        }
    }

    private fun createUser(email: String, password: String, name: String, university: String) {
        binding.signupButton.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userMap = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "university" to university,
                        "createdAt" to System.currentTimeMillis()
                    )

                    user?.let {
                        db.collection("users").document(it.uid)
                            .set(userMap)
                            .addOnSuccessListener {
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                binding.signupButton.isEnabled = true
                                Toast.makeText(this, "사용자 정보 저장에 실패했습니다: ${e.message}", 
                                    Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    binding.signupButton.isEnabled = true
                    Toast.makeText(this, "회원가입에 실패했습니다: ${task.exception?.message}", 
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun isEducationalEmail(email: String): Boolean {
        return email.endsWith(".edu") || 
               email.endsWith(".ac.kr") || 
               email.endsWith(".edu.kr")
    }
} 
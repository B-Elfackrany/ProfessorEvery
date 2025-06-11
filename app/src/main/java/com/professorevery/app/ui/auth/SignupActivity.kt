package com.professorevery.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.professorevery.app.R
import com.professorevery.app.databinding.ActivitySignupBinding
import com.professorevery.app.ui.main.MainActivity

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply saved language preference
        applyLanguagePreference()
        
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
                Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isEducationalEmail(email)) {
                Toast.makeText(this, getString(R.string.educational_email_only), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, getString(R.string.password_min_length), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            createUser(email, password, name, university)
        }
        
        binding.languageToggleButton.setOnClickListener {
            toggleLanguage()
        }
    }

    private fun applyLanguagePreference() {
        val sharedPref = getSharedPreferences("app_preferences", MODE_PRIVATE)
        val isKorean = sharedPref.getBoolean("is_korean", false) // Default to English
        
        val locale = if (isKorean) java.util.Locale("ko") else java.util.Locale("en")
        java.util.Locale.setDefault(locale)
        val config = android.content.res.Configuration()
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun toggleLanguage() {
        val sharedPref = getSharedPreferences("app_preferences", MODE_PRIVATE)
        val isCurrentlyKorean = sharedPref.getBoolean("is_korean", false)
        
        // Toggle the language preference
        with(sharedPref.edit()) {
            putBoolean("is_korean", !isCurrentlyKorean)
            apply()
        }
        
        // Show a toast message and restart activity
        val message = if (isCurrentlyKorean) {
            "🌐 Language changed to English"
        } else {
            "🌐 언어가 한국어로 변경되었습니다"
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        recreate()
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
                                Toast.makeText(this, getString(R.string.user_info_save_failed, e.message), 
                                    Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    binding.signupButton.isEnabled = true
                    Toast.makeText(this, getString(R.string.signup_failed, task.exception?.message), 
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
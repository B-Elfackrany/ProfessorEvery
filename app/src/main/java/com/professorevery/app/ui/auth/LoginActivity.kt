package com.professorevery.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.professorevery.app.R
import com.professorevery.app.databinding.ActivityLoginBinding
import com.professorevery.app.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply saved language preference
        applyLanguagePreference()
        
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Check if user is already signed in
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.login_email_password_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isEducationalEmail(email)) {
                Toast.makeText(this, getString(R.string.educational_email_only), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }

        binding.signupButton.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
        
        binding.languageToggleButton.setOnClickListener {
            toggleLanguage()
        }
    }

    private fun loginUser(email: String, password: String) {
        binding.loginButton.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                binding.loginButton.isEnabled = true
                
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, getString(R.string.login_failed, task.exception?.message), 
                        Toast.LENGTH_SHORT).show()
                }
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

    private fun isEducationalEmail(email: String): Boolean {
        return email.endsWith(".edu") || 
               email.endsWith(".ac.kr") || 
               email.endsWith(".edu.kr")
    }
} 
package com.sbuddy.app.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.sbuddy.app.BaseActivity
import com.sbuddy.app.MainActivity
import com.sbuddy.app.data.repository.AuthRepository
import com.sbuddy.app.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity() {

    private val authRepository = AuthRepository()
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.login.setOnClickListener {
            val email = binding.username.text.toString()
            val password = binding.password.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                binding.login.isEnabled = false
                lifecycleScope.launch {
                    val result = authRepository.login(email, password)
                    if (result.isSuccess) {
                        Toast.makeText(this@LoginActivity, "Welcome $email", Toast.LENGTH_SHORT).show()
                        navigateToMain()
                    } else {
                        binding.login.isEnabled = true
                        val error = result.exceptionOrNull()?.message ?: "Unknown error"
                        Toast.makeText(this@LoginActivity, "Login Failed: $error", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        binding.signUpPrompt.setOnClickListener {
             startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

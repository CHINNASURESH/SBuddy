package com.sbuddy.app.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sbuddy.app.BaseActivity
import com.sbuddy.app.MainActivity
import com.sbuddy.app.data.repository.AuthRepository
import com.sbuddy.app.databinding.ActivityLoginBinding

class LoginActivity : BaseActivity() {

    private val authRepository = AuthRepository()
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.login.setOnClickListener {
            val username = binding.username.text.toString()
            val password = binding.password.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                // Placeholder authentication logic
                Toast.makeText(this, "Welcome $username", Toast.LENGTH_SHORT).show()
                navigateToMain()
            } else {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
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

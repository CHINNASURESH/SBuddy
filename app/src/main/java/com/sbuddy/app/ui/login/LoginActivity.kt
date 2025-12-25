package com.sbuddy.app.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sbuddy.app.MainActivity
import com.sbuddy.app.R
import com.sbuddy.app.data.repository.AuthRepository

class LoginActivity : AppCompatActivity() {

    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.login)
        val guestButton = findViewById<Button>(R.id.guest_login)

        loginButton.setOnClickListener {
            // Placeholder authentication logic
            if (username.text.toString().isNotEmpty() && password.text.toString().isNotEmpty()) {
               Toast.makeText(this, "Welcome " + username.text, Toast.LENGTH_SHORT).show()
               navigateToMain()
            } else {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            }
        }

        guestButton.setOnClickListener {
             navigateToMain()
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

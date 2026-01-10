package com.sbuddy.app.ui.login

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.sbuddy.app.BaseActivity
import com.sbuddy.app.R

class SignUpActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val inputEmail = findViewById<EditText>(R.id.signup_email)
        val inputPassword = findViewById<EditText>(R.id.signup_password)
        val btnSignUp = findViewById<Button>(R.id.btn_sign_up)

        btnSignUp.setOnClickListener {
            val email = inputEmail.text.toString()
            val pass = inputPassword.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                Toast.makeText(this, "Sign Up Successful! Please Login.", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

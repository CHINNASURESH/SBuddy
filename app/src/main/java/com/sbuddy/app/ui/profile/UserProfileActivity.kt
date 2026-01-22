package com.sbuddy.app.ui.profile

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.sbuddy.app.BaseActivity
import com.sbuddy.app.R
import com.sbuddy.app.data.repository.AuthRepository
import kotlinx.coroutines.launch

class UserProfileActivity : BaseActivity() {

    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        val inputName = findViewById<EditText>(R.id.profile_name)
        val inputEmail = findViewById<EditText>(R.id.profile_email)
        val inputMobile = findViewById<EditText>(R.id.profile_mobile)
        val btnSave = findViewById<Button>(R.id.btn_save_profile)
        val btnResetPass = findViewById<Button>(R.id.btn_reset_password)
        val radioGroupTheme = findViewById<RadioGroup>(R.id.radio_group_theme)

        // Load User Data
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            inputName.setText(currentUser.displayName)
            inputEmail.setText(currentUser.email)
            inputMobile.setText(currentUser.mobile)
        } else {
             Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
             finish()
             return
        }

        btnSave.setOnClickListener {
            val newName = inputName.text.toString().trim()
            val newMobile = inputMobile.text.toString().trim()

            if (newName.isNotEmpty()) {
                val updatedUser = currentUser.copy(
                    displayName = newName,
                    mobile = newMobile
                )
                lifecycleScope.launch {
                    val result = authRepository.updateProfile(updatedUser)
                    if (result.isSuccess) {
                        Toast.makeText(this@UserProfileActivity, "Profile Updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@UserProfileActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Nick Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        btnResetPass.setOnClickListener {
             val email = currentUser.email
             if (!email.isNullOrEmpty()) {
                 lifecycleScope.launch {
                     val result = authRepository.sendPasswordResetEmail(email)
                     if (result.isSuccess) {
                         Toast.makeText(this@UserProfileActivity, "Password reset link sent to $email", Toast.LENGTH_LONG).show()
                     } else {
                         Toast.makeText(this@UserProfileActivity, "Failed to send reset link", Toast.LENGTH_SHORT).show()
                     }
                 }
             } else {
                 Toast.makeText(this, "No email found for user", Toast.LENGTH_SHORT).show()
             }
        }

        // Theme Selection
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val currentTheme = prefs.getString("selected_theme", "Default")

        when(currentTheme) {
            "Blue" -> radioGroupTheme.check(R.id.radio_blue)
            "Pink" -> radioGroupTheme.check(R.id.radio_pink)
            "Green" -> radioGroupTheme.check(R.id.radio_green)
            "Yellow" -> radioGroupTheme.check(R.id.radio_yellow)
            else -> radioGroupTheme.check(R.id.radio_default)
        }

        radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            val newTheme = when(checkedId) {
                R.id.radio_blue -> "Blue"
                R.id.radio_pink -> "Pink"
                R.id.radio_green -> "Green"
                R.id.radio_yellow -> "Yellow"
                else -> "Default"
            }
            if (newTheme != currentTheme) {
                prefs.edit().putString("selected_theme", newTheme).apply()
                recreate() // Reload activity to apply theme
            }
        }
    }
}

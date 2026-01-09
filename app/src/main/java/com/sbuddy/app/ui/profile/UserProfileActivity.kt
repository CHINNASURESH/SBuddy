package com.sbuddy.app.ui.profile

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import com.sbuddy.app.BaseActivity
import com.sbuddy.app.R

class UserProfileActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        val inputName = findViewById<EditText>(R.id.profile_name)
        val inputEmail = findViewById<EditText>(R.id.profile_email)
        val inputMobile = findViewById<EditText>(R.id.profile_mobile)
        val btnResetPass = findViewById<Button>(R.id.btn_reset_password)
        val radioGroupTheme = findViewById<RadioGroup>(R.id.radio_group_theme)

        // Mock Data
        inputName.setText("John Doe")
        inputEmail.setText("john@example.com")
        inputMobile.setText("123-456-7890")

        btnResetPass.setOnClickListener {
            Toast.makeText(this, "Password reset link sent!", Toast.LENGTH_SHORT).show()
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

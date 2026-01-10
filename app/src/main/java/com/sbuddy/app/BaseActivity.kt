package com.sbuddy.app

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs: SharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val theme = prefs.getString("selected_theme", "Default")

        when (theme) {
            "Blue" -> setTheme(R.style.Theme_SBuddy_Blue)
            "Pink" -> setTheme(R.style.Theme_SBuddy_Pink)
            "Green" -> setTheme(R.style.Theme_SBuddy_Green)
            "Yellow" -> setTheme(R.style.Theme_SBuddy_Yellow)
            else -> setTheme(R.style.Theme_SBuddy)
        }

        super.onCreate(savedInstanceState)
    }
}

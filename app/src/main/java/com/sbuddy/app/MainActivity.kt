package com.sbuddy.app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sbuddy.app.ui.buddy.BuddyGroupActivity
import com.sbuddy.app.ui.history.MatchHistoryActivity
import com.sbuddy.app.ui.login.LoginActivity
import com.sbuddy.app.ui.scoring.ScoreActivity
import com.sbuddy.app.ui.tournament.TournamentActivity

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_login).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        findViewById<Button>(R.id.btn_score_match).setOnClickListener {
            startActivity(Intent(this, ScoreActivity::class.java))
        }

        findViewById<android.view.View>(R.id.card_history).setOnClickListener {
            startActivity(Intent(this, MatchHistoryActivity::class.java))
        }

        findViewById<android.view.View>(R.id.card_tournaments).setOnClickListener {
            startActivity(Intent(this, TournamentActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0, 1, 0, "Settings")
        menu?.add(0, 2, 1, "History")
        menu?.add(0, 3, 2, "Buddy Group")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
            2 -> startActivity(Intent(this, MatchHistoryActivity::class.java))
            3 -> startActivity(Intent(this, BuddyGroupActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }
}

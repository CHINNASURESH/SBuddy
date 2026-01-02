package com.sbuddy.app

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.sbuddy.app.ui.buddy.BuddyGroupActivity
import com.sbuddy.app.ui.history.MatchHistoryActivity
import com.sbuddy.app.ui.tournament.TournamentActivity

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.app_name, R.string.app_name
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        // Card Clicks
        findViewById<android.view.View>(R.id.card_new_game).setOnClickListener {
            startActivity(Intent(this, MatchSetupActivity::class.java))
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
    }
}

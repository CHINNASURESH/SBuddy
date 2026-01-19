package com.sbuddy.app

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.sbuddy.app.ui.scoring.MatchSetupActivity
import com.sbuddy.app.ui.history.MatchHistoryActivity
import com.sbuddy.app.ui.tournament.TournamentActivity
import com.sbuddy.app.ui.group.BuddyGroupActivity
import com.sbuddy.app.ui.profile.UserProfileActivity

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

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

        findViewById<android.view.View>(R.id.card_buddy_groups).setOnClickListener {
            startActivity(Intent(this, BuddyGroupActivity::class.java))
        }

        // Mock profile icon click (Top right icon in dashboard xml)
        // Since we don't have a direct ID for it in the previous xml or it was just an imageview,
        // let's assume it's part of the toolbar or a specific view.
        // Assuming we need to add a menu item or a view click.
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_profile) {
            startActivity(Intent(this, UserProfileActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_dashboard -> {
                // Already here
            }
            R.id.nav_new_game -> {
                startActivity(Intent(this, MatchSetupActivity::class.java))
            }
            R.id.nav_history -> {
                startActivity(Intent(this, MatchHistoryActivity::class.java))
            }
            R.id.nav_tournaments -> {
                startActivity(Intent(this, TournamentActivity::class.java))
            }
            R.id.nav_buddy_groups -> {
                startActivity(Intent(this, BuddyGroupActivity::class.java))
            }
            R.id.nav_profile -> {
                startActivity(Intent(this, UserProfileActivity::class.java))
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}

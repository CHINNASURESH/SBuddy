package com.sbuddy.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.sbuddy.app.ui.scoring.ScoreActivity
import com.sbuddy.app.ui.tournament.TournamentActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<android.view.View>(R.id.card_new_game).setOnClickListener {
            // Navigate to Match Setup instead of direct score
            startActivity(Intent(this, com.sbuddy.app.ui.scoring.MatchSetupActivity::class.java))
        }

        findViewById<android.view.View>(R.id.card_history).setOnClickListener {
            startActivity(Intent(this, com.sbuddy.app.ui.history.MatchHistoryActivity::class.java))
        }

        findViewById<android.view.View>(R.id.card_tournaments).setOnClickListener {
            startActivity(Intent(this, TournamentActivity::class.java))
        }
    }
}

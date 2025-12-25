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

        findViewById<Button>(R.id.btn_score_match).setOnClickListener {
            startActivity(Intent(this, ScoreActivity::class.java))
        }

        findViewById<Button>(R.id.btn_tournament).setOnClickListener {
            startActivity(Intent(this, TournamentActivity::class.java))
        }
    }
}

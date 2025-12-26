package com.sbuddy.app.ui.scoring

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sbuddy.app.R
import com.sbuddy.app.utils.GameLogic

class ScoreActivity : AppCompatActivity() {

    private var scoreP1 = 0
    private var scoreP2 = 0
    private val gameLogic = GameLogic()

    // For simplicity, assuming P1 starts serving
    private var currentServer = "Player 1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)

        val txtScoreP1 = findViewById<TextView>(R.id.score_p1)
        val txtScoreP2 = findViewById<TextView>(R.id.score_p2)
        val txtServiceInfo = findViewById<TextView>(R.id.service_info)

        val btnP1Add = findViewById<Button>(R.id.btn_p1_add)
        val btnP2Add = findViewById<Button>(R.id.btn_p2_add)

        fun updateUI() {
            txtScoreP1.text = scoreP1.toString()
            txtScoreP2.text = scoreP2.toString()

            // Determine who is serving based on who won the last point
            // This is a simplified logic. In real badminton, if server wins point, they keep serving.
            // If receiver wins point, they become server.
            // We need to track who was the LAST server to know who is CURRENTLY serving if we want perfect logic.

            // AI Server Highlighting
            // Just displaying the rule-based position for the current server
            val servingScore = if (currentServer == "Player 1") scoreP1 else scoreP2
            txtServiceInfo.text = gameLogic.getServiceStatus(currentServer, servingScore)
        }

        btnP1Add.setOnClickListener {
            scoreP1++
            // If P1 was serving, they continue serving.
            // If P2 was serving, P1 becomes server.
            currentServer = "Player 1"
            updateUI()
        }

        btnP2Add.setOnClickListener {
            scoreP2++
            currentServer = "Player 2"
            updateUI()
        }

        updateUI()
    }
}

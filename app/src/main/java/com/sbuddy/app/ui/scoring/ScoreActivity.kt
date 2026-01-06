package com.sbuddy.app.ui.scoring

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.sbuddy.app.R
import com.sbuddy.app.utils.GameLogic

class ScoreActivity : AppCompatActivity() {

    private var scoreP1 = 0
    private var scoreP2 = 0
    private val gameLogic = GameLogic()
    private var currentServer = "Team 1" // Tracks which team is serving

    // Configuration from MatchSetup
    private var maxScore = 21
    private var team1Name = "Team 1"
    private var team2Name = "Team 2"
    private var isSingles = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)

        // Get extras
        maxScore = intent.getIntExtra("MAX_SCORE", 21)
        team1Name = intent.getStringExtra("TEAM_1_NAME") ?: "Team 1"
        team2Name = intent.getStringExtra("TEAM_2_NAME") ?: "Team 2"
        isSingles = intent.getBooleanExtra("IS_SINGLES", false)

        val txtInfo = findViewById<TextView>(R.id.txt_match_info)
        val typeStr = if (isSingles) "Singles" else "Doubles"
        txtInfo.text = "$typeStr Match • First to $maxScore"

        val txtTeam1 = findViewById<TextView>(R.id.txt_team1_name)
        val txtTeam2 = findViewById<TextView>(R.id.txt_team2_name)
        txtTeam1.text = team1Name
        txtTeam2.text = team2Name

        val txtScoreP1 = findViewById<TextView>(R.id.score_p1)
        val txtScoreP2 = findViewById<TextView>(R.id.score_p2)

        val btnP1Add = findViewById<Button>(R.id.btn_p1_add)
        val btnP1Minus = findViewById<Button>(R.id.btn_p1_minus)
        val btnP2Add = findViewById<Button>(R.id.btn_p2_add)
        val btnP2Minus = findViewById<Button>(R.id.btn_p2_minus)
        val btnReset = findViewById<Button>(R.id.btn_reset)

        fun updateUI() {
            txtScoreP1.text = scoreP1.toString()
            txtScoreP2.text = scoreP2.toString()

            val cardTeam1 = findViewById<CardView>(R.id.card_team1)
            val cardTeam2 = findViewById<CardView>(R.id.card_team2)
            val lblServingT1 = findViewById<TextView>(R.id.lbl_serving_t1)
            val lblServingT2 = findViewById<TextView>(R.id.lbl_serving_t2)

            // Visual logic for serving
            // If T1 is serving: T1 card purple, text white. T2 card white, text black.
            if (currentServer == "Team 1") {
                cardTeam1.setCardBackgroundColor(Color.parseColor("#E1BEE7")) // Light Purple
                cardTeam2.setCardBackgroundColor(Color.WHITE)
                lblServingT1.visibility = View.VISIBLE
                lblServingT2.visibility = View.INVISIBLE
            } else {
                cardTeam1.setCardBackgroundColor(Color.WHITE)
                cardTeam2.setCardBackgroundColor(Color.parseColor("#E1BEE7"))
                lblServingT1.visibility = View.INVISIBLE
                lblServingT2.visibility = View.VISIBLE
            }
        }

        fun checkGameOver() {
            if (scoreP1 >= maxScore || scoreP2 >= maxScore) {
                // Simplified win condition: reach max score (badminton usually needs 2 point lead)
                // For this task, we assume strict "Points to Win"
                val winner = if (scoreP1 > scoreP2) team1Name else team2Name
                showGameOverDialog(winner, "$scoreP1 - $scoreP2")
            }
        }

        btnP1Add.setOnClickListener {
            scoreP1++
            // In badminton, if serving side wins point, they keep serving.
            // If receiving side wins point, they become server.
            // Assuming simplified logic: if P1 scored, they might become server if they weren't.
            // Actually, let's just say whoever scores becomes/stays server for visual simplicity if not specified.
            currentServer = "Team 1"
            updateUI()
            checkGameOver()
        }

        btnP1Minus.setOnClickListener {
            if (scoreP1 > 0) {
                scoreP1--
                updateUI()
            }
        }

        btnP2Add.setOnClickListener {
            scoreP2++
            currentServer = "Team 2"
            updateUI()
            checkGameOver()
        }

        btnP2Minus.setOnClickListener {
             if (scoreP2 > 0) {
                scoreP2--
                updateUI()
            }
        }

        btnReset.setOnClickListener {
            scoreP1 = 0
            scoreP2 = 0
            currentServer = "Team 1"
            updateUI()
        }

        updateUI()
    }

    private fun showGameOverDialog(winnerName: String, finalScore: String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("🏆 Game Over!")
            .setMessage("$winnerName wins the game!\nFinal Score: $finalScore")
            .setCancelable(false)
            .setPositiveButton("New Game") { _, _ ->
                finish() // Go back to setup
            }
            .setNegativeButton("Rematch") { _, _ ->
                scoreP1 = 0
                scoreP2 = 0
                // Keep server same or swap? Let's reset to T1
                currentServer = "Team 1"
                // Refresh UI
                val txtScoreP1 = findViewById<TextView>(R.id.score_p1)
                val txtScoreP2 = findViewById<TextView>(R.id.score_p2)
                txtScoreP1.text = "0"
                txtScoreP2.text = "0"
            }
            .create()
        dialog.show()
    }
}

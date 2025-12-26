package com.sbuddy.app.ui.scoring

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sbuddy.app.R
import com.sbuddy.app.utils.GameLogic

class ScoreActivity : AppCompatActivity() {

    private val gameLogic = GameLogic()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)

        val txtScoreP1 = findViewById<TextView>(R.id.score_p1)
        val txtScoreP2 = findViewById<TextView>(R.id.score_p2)
        val txtServiceInfo = findViewById<TextView>(R.id.service_info)

        val btnP1Add = findViewById<Button>(R.id.btn_p1_add)
        val btnP2Add = findViewById<Button>(R.id.btn_p2_add)
        val btnUndo = findViewById<Button>(R.id.btn_undo)

        fun updateUI() {
            txtScoreP1.text = gameLogic.getScoreP1().toString()
            txtScoreP2.text = gameLogic.getScoreP2().toString()
            txtServiceInfo.text = gameLogic.getServiceStatus()
        }

        btnP1Add.setOnClickListener {
            gameLogic.addPoint("Player 1")
            updateUI()
        }

        btnP2Add.setOnClickListener {
            gameLogic.addPoint("Player 2")
            updateUI()
        }

        btnUndo.setOnClickListener {
            gameLogic.undo()
            updateUI()
        }

        updateUI()
    }
}

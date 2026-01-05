package com.sbuddy.app.ui.scoring

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.sbuddy.app.R
import com.sbuddy.app.utils.GameLogic

class ScoreActivity : AppCompatActivity() {

    private val gameLogic = GameLogic()
    private lateinit var txtScoreP1: TextView
    private lateinit var txtScoreP2: TextView
    private lateinit var txtServiceInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)

        txtScoreP1 = findViewById(R.id.score_p1)
        txtScoreP2 = findViewById(R.id.score_p2)
        txtServiceInfo = findViewById(R.id.service_info)

        val btnP1Add = findViewById<Button>(R.id.btn_p1_add)
        val btnP1Minus = findViewById<Button>(R.id.btn_p1_minus)
        val btnP2Add = findViewById<Button>(R.id.btn_p2_add)
        val btnUndo = findViewById<Button>(R.id.btn_undo)

        btnP1Add.setOnClickListener {
            gameLogic.addPoint(gameLogic.getP1Name())
            updateUI()
        }

        btnP2Add.setOnClickListener {
            gameLogic.addPoint(gameLogic.getP2Name())
            updateUI()
        }

        btnUndo.setOnClickListener {
            gameLogic.undo()
            updateUI()
        }

        showSetupDialog()
    }

    private fun showSetupDialog() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val inputP1 = EditText(this)
        inputP1.hint = "Player 1 Name"
        layout.addView(inputP1)

        val inputP2 = EditText(this)
        inputP2.hint = "Player 2 Name"
        layout.addView(inputP2)

        val checkDoubles = CheckBox(this)
        checkDoubles.text = "Doubles Match"
        checkDoubles.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                inputP1.hint = "Team 1 Name"
                inputP2.hint = "Team 2 Name"
            } else {
                inputP1.hint = "Player 1 Name"
                inputP2.hint = "Player 2 Name"
            }
        }
        layout.addView(checkDoubles)

        val radioGroup = RadioGroup(this)
        radioGroup.orientation = RadioGroup.HORIZONTAL
        val rb15 = RadioButton(this); rb15.text = "15"; radioGroup.addView(rb15)
        val rb21 = RadioButton(this); rb21.text = "21"; rb21.isChecked = true; radioGroup.addView(rb21)
        val rb30 = RadioButton(this); rb30.text = "30"; radioGroup.addView(rb30)
        layout.addView(radioGroup)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Match Setup")
            .setView(layout)
            .setCancelable(false)
            .setPositiveButton("Start Match") { _, _ ->
                val p1 = inputP1.text.toString()
                val p2 = inputP2.text.toString()
                val isDoubles = checkDoubles.isChecked
                val points = when {
                    rb15.isChecked -> 15
                    rb30.isChecked -> 30
                    else -> 21
                }
                gameLogic.setRules(points, p1, p2, isDoubles)
                updateUI()
            }
            .create()

        dialog.show()
    }

    private fun updateUI() {
        txtScoreP1.text = gameLogic.getScoreP1().toString()
        txtScoreP2.text = gameLogic.getScoreP2().toString()
        txtServiceInfo.text = gameLogic.getServiceStatus()
    }
}

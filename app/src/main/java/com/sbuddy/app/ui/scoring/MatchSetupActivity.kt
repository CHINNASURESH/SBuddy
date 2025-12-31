package com.sbuddy.app.ui.scoring

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.sbuddy.app.R

class MatchSetupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_setup)

        val spinnerGameType = findViewById<Spinner>(R.id.spinner_game_type)
        val spinnerPoints = findViewById<Spinner>(R.id.spinner_points)
        val inputPlayer2 = findViewById<EditText>(R.id.input_player2)
        val inputPlayer4 = findViewById<EditText>(R.id.input_player4)
        val btnStart = findViewById<Button>(R.id.btn_start_match)

        // Setup Spinners
        val gameTypes = arrayOf("Doubles", "Singles")
        spinnerGameType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, gameTypes)

        val pointsOptions = arrayOf("21 Points", "15 Points", "30 Points")
        spinnerPoints.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, pointsOptions)

        // Toggle visibility based on Game Type
        spinnerGameType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val isSingles = gameTypes[position] == "Singles"
                val visibility = if (isSingles) View.GONE else View.VISIBLE
                inputPlayer2.visibility = visibility
                inputPlayer4.visibility = visibility
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        btnStart.setOnClickListener {
            val isSingles = spinnerGameType.selectedItem.toString() == "Singles"
            val pointsString = spinnerPoints.selectedItem.toString().split(" ")[0]
            val maxPoints = pointsString.toIntOrNull() ?: 21

            val p1Name = findViewById<EditText>(R.id.input_player1).text.toString().ifEmpty { "Player 1" }
            val p2Name = findViewById<EditText>(R.id.input_player2).text.toString().ifEmpty { "Player 2" }
            val p3Name = findViewById<EditText>(R.id.input_player3).text.toString().ifEmpty { "Player 3" }
            val p4Name = findViewById<EditText>(R.id.input_player4).text.toString().ifEmpty { "Player 4" }

            val team1Name = if (isSingles) p1Name else "$p1Name & $p2Name"
            val team2Name = if (isSingles) p3Name else "$p3Name & $p4Name"

            val intent = Intent(this, ScoreActivity::class.java).apply {
                putExtra("MAX_SCORE", maxPoints)
                putExtra("TEAM_1_NAME", team1Name)
                putExtra("TEAM_2_NAME", team2Name)
                putExtra("IS_SINGLES", isSingles)
            }
            startActivity(intent)
            finish()
        }
    }
}

package com.sbuddy.app.ui.scoring

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sbuddy.app.BaseActivity
import com.sbuddy.app.R

class MatchSetupActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_setup)

        val spinnerGameType = findViewById<Spinner>(R.id.spinner_game_type)
        val spinnerPoints = findViewById<Spinner>(R.id.spinner_points)
        val inputPlayer2 = findViewById<EditText>(R.id.input_player2)
        val inputPlayer4 = findViewById<EditText>(R.id.input_player4)
        val inputCustomPoints = findViewById<EditText>(R.id.input_custom_points)
        val btnStart = findViewById<Button>(R.id.btn_start_match)

        // Setup Spinners
        val gameTypes = arrayOf("Doubles", "Singles")
        spinnerGameType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, gameTypes)

        val pointsOptions = arrayOf("21 (Default)", "15", "30", "Custom")
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

        // Toggle Custom Points input
        spinnerPoints.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val isCustom = pointsOptions[position] == "Custom"
                inputCustomPoints.visibility = if (isCustom) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        btnStart.setOnClickListener {
            val isSingles = spinnerGameType.selectedItem.toString() == "Singles"
            val pointsSelection = spinnerPoints.selectedItem.toString()

            var maxPoints = 21
            if (pointsSelection == "Custom") {
                val customStr = inputCustomPoints.text.toString()
                if (customStr.isEmpty()) {
                    Toast.makeText(this, "Please enter custom points", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                maxPoints = customStr.toIntOrNull() ?: 21
            } else {
                // Parse "21 (Default)", "15", "30"
                maxPoints = pointsSelection.split(" ")[0].toIntOrNull() ?: 21
            }

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
                putExtra("PLAYER_1_NAME", p1Name)
                putExtra("PLAYER_2_NAME", p2Name)
                putExtra("PLAYER_3_NAME", p3Name)
                putExtra("PLAYER_4_NAME", p4Name)
            }
            startActivity(intent)
            finish()
        }
    }
}

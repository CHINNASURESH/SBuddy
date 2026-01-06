package com.sbuddy.app.ui.tournament

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sbuddy.app.R
import com.sbuddy.app.utils.TournamentManager

class TournamentActivity : AppCompatActivity() {

    private val participants = mutableListOf<String>()
    private val tournamentManager = TournamentManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournament)

        val inputName = findViewById<EditText>(R.id.input_player_name)
        val btnAdd = findViewById<Button>(R.id.btn_add_player)
        val btnGenerate = findViewById<Button>(R.id.btn_generate_random)
        val txtCount = findViewById<TextView>(R.id.txt_participants_count)
        val txtBracket = findViewById<TextView>(R.id.txt_bracket)

        btnAdd.setOnClickListener {
            val name = inputName.text.toString().trim()
            if (name.isNotEmpty()) {
                participants.add(name)
                inputName.text.clear()
                txtCount.text = "Participants: ${participants.size}"
                Toast.makeText(this, "Added $name", Toast.LENGTH_SHORT).show()
            }
        }

        btnGenerate.setOnClickListener {
            if (participants.size < 2) {
                Toast.makeText(this, "Need at least 2 participants", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fixtures = tournamentManager.generateFixtures(participants)
            val sb = StringBuilder()

            sb.append("Round 1 Fixtures:\n\n")
            fixtures.forEachIndexed { index, match ->
                sb.append("Match ${index + 1}:\n")
                if (match.player2Name == "BYE") {
                    sb.append("${match.player1Name} gets a BYE\n")
                } else {
                    sb.append("${match.player1Name} vs ${match.player2Name}\n")
                }
                sb.append("\n")
            }

            txtBracket.text = sb.toString()
            txtBracket.gravity = android.view.Gravity.START
        }
    }
}

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

        val etName = findViewById<EditText>(R.id.et_participant_name)
        val cbSeed = findViewById<android.widget.CheckBox>(R.id.cb_top_seed)
        val btnAdd = findViewById<Button>(R.id.btn_add_participant)
        val txtList = findViewById<TextView>(R.id.txt_participants_list)
        val btnGenerate = findViewById<Button>(R.id.btn_generate_fixtures)
        val txtFixtures = findViewById<TextView>(R.id.txt_fixtures)

        btnAdd.setOnClickListener {
            var name = etName.text.toString().trim()
            if (name.isNotEmpty()) {
                if (cbSeed.isChecked) {
                    name += " (Seed)"
                }
                participants.add(name)
                etName.text.clear()
                cbSeed.isChecked = false
                updateListUI(txtList)
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

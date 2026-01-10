package com.sbuddy.app.ui.tournament

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sbuddy.app.BaseActivity
import com.sbuddy.app.R
import com.sbuddy.app.utils.TournamentManager

class TournamentActivity : BaseActivity() {

    private val participants = mutableListOf<String>()
    private val tournamentManager = TournamentManager()
    private var topSeed: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournament)

        val inputName = findViewById<EditText>(R.id.input_player_name)
        val btnAdd = findViewById<Button>(R.id.btn_add_player)
        val btnGenerate = findViewById<Button>(R.id.btn_generate_fixtures)
        val txtCount = findViewById<TextView>(R.id.txt_participants_count)
        val txtBracket = findViewById<TextView>(R.id.txt_bracket)
        val checkSeed = findViewById<CheckBox>(R.id.check_top_seed)
        val btnShare = findViewById<ImageButton>(R.id.btn_share_fixtures)

        val inputTournamentName = findViewById<EditText>(R.id.input_tournament_name)
        val inputCategory = findViewById<EditText>(R.id.input_tournament_category)
        val checkPublic = findViewById<CheckBox>(R.id.check_public)
        val spinnerType = findViewById<Spinner>(R.id.spinner_tournament_type)

        // Setup Spinner
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Knockout", "Round Robin")
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter

        btnAdd.setOnClickListener {
            val name = inputName.text.toString().trim()
            if (name.isNotEmpty()) {
                if (participants.contains(name)) {
                    Toast.makeText(this, "Already added", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                participants.add(name)

                if (checkSeed.isChecked) {
                    topSeed = name
                    Toast.makeText(this, "Added $name as Top Seed", Toast.LENGTH_SHORT).show()
                    checkSeed.isChecked = false
                } else {
                    Toast.makeText(this, "Added $name", Toast.LENGTH_SHORT).show()
                }

                inputName.text.clear()
                txtCount.text = "Participants: ${participants.size}"
            }
        }

        btnGenerate.setOnClickListener {
            if (participants.size < 2) {
                Toast.makeText(this, "Need at least 2 participants", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedType = spinnerType.selectedItem as String
            val bracketText = if (selectedType == "Round Robin") {
                tournamentManager.generateRoundRobinText(participants)
            } else {
                tournamentManager.generateBracketText(participants, topSeed)
            }

            txtBracket.text = bracketText
            txtBracket.gravity = android.view.Gravity.START

            if (checkPublic.isChecked) {
                Toast.makeText(this, "Tournament is now Public!", Toast.LENGTH_SHORT).show()
            }
        }

        btnShare.setOnClickListener {
            val bracket = txtBracket.text.toString()
            if (bracket.isEmpty() || bracket.contains("Add players")) {
                Toast.makeText(this, "Generate fixtures first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tName = inputTournamentName.text.toString().ifEmpty { "Tournament" }
            val shareText = "SBuddy - $tName Fixtures:\n\n$bracket"

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, shareText)
            startActivity(Intent.createChooser(intent, "Share Fixtures"))
        }
    }
}

package com.sbuddy.app.ui.tournament

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sbuddy.app.BaseActivity
import com.sbuddy.app.R
import com.sbuddy.app.data.model.Match
import com.sbuddy.app.data.model.Tournament
import com.sbuddy.app.data.repository.AuthRepository
import com.sbuddy.app.data.repository.TournamentRepository
import com.sbuddy.app.utils.TournamentManager
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch

class TournamentFixturesActivity : BaseActivity() {

    private val participants = mutableListOf<String>()
    private val tournamentManager = TournamentManager()
    private val tournamentRepository = TournamentRepository()
    private val authRepository = AuthRepository()
    private var topSeed: String? = null
    private var currentTournamentId: String = ""
    private var currentTournament: Tournament? = null
    private var isDoublesMode = false

    private val rounds = mutableListOf<Match>()
    private lateinit var fixtureAdapter: FixtureAdapter
    private lateinit var scoreLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val REQUEST_IMPORT = 202
        private const val REQUEST_CREATE_FILE = 203
        private const val REQUEST_PERMISSION_STORAGE = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournament_fixtures)

        currentTournamentId = intent.getStringExtra("TOURNAMENT_ID") ?: ""
        if (currentTournamentId.isEmpty()) {
            Toast.makeText(this, "Tournament ID missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        loadTournament()
    }

    private fun setupUI() {
        val inputName = findViewById<EditText>(R.id.input_player_name)
        val inputName2 = findViewById<EditText>(R.id.input_player_name_2)
        val btnAdd = findViewById<Button>(R.id.btn_add_player)
        val btnGenerate = findViewById<Button>(R.id.btn_generate_fixtures)
        val btnSave = findViewById<Button>(R.id.btn_finish_save)
        val txtCount = findViewById<TextView>(R.id.txt_participants_count)
        val txtBracket = findViewById<EditText>(R.id.txt_bracket)
        val checkSeed = findViewById<CheckBox>(R.id.check_top_seed)
        val btnShare = findViewById<ImageButton>(R.id.btn_share_fixtures)
        val btnEdit = findViewById<ImageButton>(R.id.btn_edit_fixtures)
        val btnImport = findViewById<ImageButton>(R.id.btn_import_excel)
        val btnDownload = findViewById<ImageButton>(R.id.btn_download_excel)

        val scrollBracket = findViewById<View>(R.id.scroll_bracket_text)
        val recyclerFixtures = findViewById<RecyclerView>(R.id.recycler_fixtures)
        val btnToggle = findViewById<ImageButton>(R.id.btn_toggle_view)

        // Default to Read-Only
        txtBracket.focusable = View.NOT_FOCUSABLE
        txtBracket.isFocusableInTouchMode = false

        // Init RecyclerView
        recyclerFixtures.layoutManager = LinearLayoutManager(this)
        fixtureAdapter = FixtureAdapter(onScoreClick = { match ->
            val intent = Intent(this, com.sbuddy.app.ui.scoring.ScoreActivity::class.java)
            intent.putExtra("MATCH_ID", match.id)
            intent.putExtra("MATCH_LABEL", match.matchLabel)
            intent.putExtra("TOURNAMENT_ID", currentTournamentId)
            intent.putExtra("MAX_SCORE", 21) // Default

            intent.putExtra("TEAM_1_NAME", match.player1Name)
            intent.putExtra("TEAM_2_NAME", match.player2Name)
            // Assuming we pass IS_SINGLES based on tournament logic, but for now we need to infer or load it.
            // We'll set it when loading tournament.
            intent.putExtra("IS_SINGLES", !isDoublesMode)

            scoreLauncher.launch(intent)
        })
        recyclerFixtures.adapter = fixtureAdapter

        // Init Score Launcher
        scoreLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val matchId = result.data?.getStringExtra("MATCH_ID")
                val winner = result.data?.getStringExtra("WINNER")
                val s1 = result.data?.getIntExtra("SCORE_P1", 0) ?: 0
                val s2 = result.data?.getIntExtra("SCORE_P2", 0) ?: 0

                if (matchId != null && winner != null) {
                    updateMatchResult(matchId, winner, s1, s2)
                }
            }
        }

        btnToggle.setOnClickListener {
            if (recyclerFixtures.visibility == View.VISIBLE) {
                recyclerFixtures.visibility = View.GONE
                scrollBracket.visibility = View.VISIBLE
                Toast.makeText(this, "Text View", Toast.LENGTH_SHORT).show()
            } else {
                recyclerFixtures.visibility = View.VISIBLE
                scrollBracket.visibility = View.GONE
                Toast.makeText(this, "Interactive View", Toast.LENGTH_SHORT).show()
            }
        }

        btnAdd.setOnClickListener {
            var name = inputName.text.toString().trim()
            val name2 = inputName2.text.toString().trim()

            if (isDoublesMode) {
                if (name.isNotEmpty() && name2.isNotEmpty()) {
                    name = "$name & $name2"
                } else {
                    Toast.makeText(this, "Both Player 1 and Player 2 required for Doubles", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

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
                inputName2.text.clear()
                txtCount.text = "Participants: ${participants.size}"
            }
        }

        btnGenerate.setOnClickListener {
            if (participants.size < 2) {
                Toast.makeText(this, "Need at least 2 participants", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedType = currentTournament?.type ?: "Knockout"

            val bracketText = if (selectedType == "League") {
                tournamentManager.generateLeagueText(participants)
            } else {
                tournamentManager.generateBracketText(participants, topSeed)
            }
            txtBracket.setText(bracketText)

            // List Generation
            val generatedRounds = tournamentManager.generateFixturesList(participants, selectedType, topSeed)
            rounds.clear()
            rounds.addAll(generatedRounds)
            fixtureAdapter.setMatches(rounds)

            // Switch to List View
            recyclerFixtures.visibility = View.VISIBLE
            scrollBracket.visibility = View.GONE
        }

        btnSave.setOnClickListener {
            saveTournament()
        }

        btnShare.setOnClickListener {
            val bracket = txtBracket.text.toString()
            if (bracket.isEmpty() || bracket.contains("Add players")) {
                Toast.makeText(this, "Generate fixtures first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tName = currentTournament?.name ?: "Tournament"
            val shareText = "SBuddy - $tName Fixtures:\n\n$bracket"

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, shareText)
            startActivity(Intent.createChooser(intent, "Share Fixtures"))
        }

        btnEdit.setOnClickListener {
            txtBracket.focusable = View.FOCUSABLE
            txtBracket.isFocusableInTouchMode = true
            txtBracket.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(txtBracket, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
            Toast.makeText(this, "Editing Enabled", Toast.LENGTH_SHORT).show()
        }

        btnImport.setOnClickListener {
            if (checkStoragePermission()) {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "*/*"
                val mimeTypes = arrayOf("text/csv", "text/comma-separated-values", "text/plain")
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                startActivityForResult(intent, REQUEST_IMPORT)
            }
        }

        btnDownload.setOnClickListener {
            if (participants.isEmpty()) {
                Toast.makeText(this, "No participants to download", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (checkStoragePermission()) {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/csv"
                    putExtra(Intent.EXTRA_TITLE, "participants.csv")
                }
                startActivityForResult(intent, REQUEST_CREATE_FILE)
            }
        }
    }

    private fun loadTournament() {
        lifecycleScope.launch {
            val result = tournamentRepository.getTournament(currentTournamentId)
            if (result.isSuccess) {
                val tournament = result.getOrNull()
                if (tournament != null) {
                    currentTournament = tournament

                    // Populate UI
                    participants.clear()
                    participants.addAll(tournament.participants)
                    findViewById<TextView>(R.id.txt_participants_count).text = "Participants: ${participants.size}"

                    rounds.clear()
                    rounds.addAll(tournament.rounds)
                    fixtureAdapter.setMatches(rounds)

                    findViewById<EditText>(R.id.txt_bracket).setText(tournament.bracketText)

                    if (rounds.isNotEmpty()) {
                        findViewById<RecyclerView>(R.id.recycler_fixtures).visibility = View.VISIBLE
                        findViewById<View>(R.id.scroll_bracket_text).visibility = View.GONE
                    }

                    isDoublesMode = tournament.mode == "Doubles"

                    if (isDoublesMode) {
                        findViewById<EditText>(R.id.input_player_name_2).visibility = View.VISIBLE
                        findViewById<EditText>(R.id.input_player_name).hint = "Player 1 Name"
                    } else {
                        findViewById<EditText>(R.id.input_player_name_2).visibility = View.GONE
                        findViewById<EditText>(R.id.input_player_name).hint = "Player Name"
                    }
                }
            } else {
                Toast.makeText(this@TournamentFixturesActivity, "Error loading tournament", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveTournament() {
        if (currentTournament == null) return

        val progressBar = findViewById<android.widget.ProgressBar>(R.id.progress_bar)
        val btnSave = findViewById<Button>(R.id.btn_finish_save)
        val txtBracket = findViewById<EditText>(R.id.txt_bracket)

        progressBar.visibility = View.VISIBLE
        btnSave.isEnabled = false

        val bracketText = txtBracket.text.toString()
        val status = if (rounds.isNotEmpty()) "In Progress" else currentTournament?.status ?: "Open"

        val updatedTournament = currentTournament!!.copy(
            participants = participants,
            bracketText = bracketText,
            rounds = rounds,
            status = status
        )

        lifecycleScope.launch {
            var result = tournamentRepository.saveTournament(updatedTournament)

            // Handle Timeout specifically by verifying if data was actually saved
            if (result.isFailure && result.exceptionOrNull() is TimeoutCancellationException) {
                 val check = tournamentRepository.getTournament(currentTournamentId)
                 if (check.isSuccess) {
                     val remote = check.getOrNull()
                     // Simple check: if participant count and round count matches, assume success
                     if (remote != null && remote.participants.size == participants.size && remote.rounds.size == rounds.size) {
                         result = Result.success(currentTournamentId)
                     }
                 }
            }

            progressBar.visibility = View.GONE
            btnSave.isEnabled = true

            if (result.isSuccess) {
                Toast.makeText(this@TournamentFixturesActivity, "Saved successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                 val ex = result.exceptionOrNull()
                 if (ex is TimeoutCancellationException || ex?.message?.contains("Timed out") == true) {
                     Toast.makeText(this@TournamentFixturesActivity, "Failed to save. Please try again.", Toast.LENGTH_LONG).show()
                 } else {
                     val msg = ex?.message ?: "Unknown error"
                     Toast.makeText(this@TournamentFixturesActivity, "Failed to save: $msg", Toast.LENGTH_LONG).show()
                 }
            }
        }
    }

    private fun updateMatchResult(matchId: String, winner: String, s1: Int, s2: Int) {
        val index = rounds.indexOfFirst { it.id == matchId }
        if (index != -1) {
            val oldMatch = rounds[index]
            val newMatch = oldMatch.copy(
                winner = winner,
                player1Score = s1,
                player2Score = s2
            )
            rounds[index] = newMatch

            // Progression Logic
            for (i in rounds.indices) {
                val m = rounds[i]
                var updated = false
                var p1 = m.player1Name
                var p2 = m.player2Name

                if (m.sourceMatchId1 == matchId) {
                    p1 = winner
                    updated = true
                }
                if (m.sourceMatchId2 == matchId) {
                    p2 = winner
                    updated = true
                }

                if (updated) {
                    rounds[i] = m.copy(player1Name = p1, player2Name = p2)
                }
            }
            fixtureAdapter.setMatches(rounds)
        }
    }

    private fun checkStoragePermission(): Boolean {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
             if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                 ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION_STORAGE)
                 return false
             }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_IMPORT -> {
                    data.data?.let { uri ->
                        try {
                            contentResolver.openInputStream(uri)?.use { inputStream ->
                                val reader = java.io.BufferedReader(java.io.InputStreamReader(inputStream))
                                var line = reader.readLine()
                                var count = 0
                                while (line != null) {
                                    val name = line.trim().replace(",", "")
                                    if (name.isNotEmpty() && !participants.contains(name)) {
                                        participants.add(name)
                                        count++
                                    }
                                    line = reader.readLine()
                                }
                                findViewById<TextView>(R.id.txt_participants_count).text = "Participants: ${participants.size}"
                                Toast.makeText(this, "Imported $count players", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this, "Error importing file", Toast.LENGTH_SHORT).show()
                            e.printStackTrace()
                        }
                    }
                }
                REQUEST_CREATE_FILE -> {
                    data.data?.let { uri ->
                        try {
                            contentResolver.openOutputStream(uri)?.use { outputStream ->
                                val writer = java.io.BufferedWriter(java.io.OutputStreamWriter(outputStream))
                                participants.forEach { name ->
                                    writer.write(name)
                                    writer.newLine()
                                }
                                writer.flush()
                                Toast.makeText(this, "List downloaded", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this, "Error saving file", Toast.LENGTH_SHORT).show()
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }
}

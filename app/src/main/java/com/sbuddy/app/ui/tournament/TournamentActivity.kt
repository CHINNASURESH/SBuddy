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
import androidx.lifecycle.lifecycleScope
import com.sbuddy.app.BaseActivity
import com.sbuddy.app.R
import com.sbuddy.app.data.model.Tournament
import com.sbuddy.app.data.repository.TournamentRepository
import com.sbuddy.app.utils.TournamentManager
import kotlinx.coroutines.launch

class TournamentActivity : BaseActivity() {

    private val participants = mutableListOf<String>()
    private val tournamentManager = TournamentManager()
    private val tournamentRepository = TournamentRepository()
    private var topSeed: String? = null
    private var selectedImageUri: android.net.Uri? = null
    private var currentTournamentId: String = ""

    companion object {
        private const val REQUEST_IMAGE = 201
        private const val REQUEST_IMPORT = 202
        private const val REQUEST_CREATE_FILE = 203
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournament)

        val inputName = findViewById<EditText>(R.id.input_player_name)
        val inputName2 = findViewById<EditText>(R.id.input_player_name_2)
        val btnAdd = findViewById<Button>(R.id.btn_add_player)
        val btnGenerate = findViewById<Button>(R.id.btn_generate_fixtures)
        val btnPublish = findViewById<Button>(R.id.btn_publish)
        val btnViewPublic = findViewById<Button>(R.id.btn_view_public)
        val txtCount = findViewById<TextView>(R.id.txt_participants_count)
        val txtBracket = findViewById<EditText>(R.id.txt_bracket)
        val checkSeed = findViewById<CheckBox>(R.id.check_top_seed)
        val btnShare = findViewById<ImageButton>(R.id.btn_share_fixtures)
        val btnEdit = findViewById<ImageButton>(R.id.btn_edit_fixtures)

        val btnSelectImage = findViewById<Button>(R.id.btn_select_image)
        val txtImageStatus = findViewById<TextView>(R.id.txt_image_status)
        val imgPreview = findViewById<android.widget.ImageView>(R.id.img_tournament_preview)
        val btnImport = findViewById<ImageButton>(R.id.btn_import_excel)
        val btnDownload = findViewById<ImageButton>(R.id.btn_download_excel)

        // Defined in XML, was missing here causing crash
        val inputTournamentName = findViewById<EditText>(R.id.input_tournament_name)
        val inputCategory = findViewById<EditText>(R.id.input_tournament_category)
        val checkPublic = findViewById<CheckBox>(R.id.check_public)
        val spinnerType = findViewById<Spinner>(R.id.spinner_tournament_type)
        val radioGroupMode = findViewById<android.widget.RadioGroup>(R.id.radio_group_mode)

        // Default to Read-Only
        txtBracket.focusable = android.view.View.NOT_FOCUSABLE
        txtBracket.isFocusableInTouchMode = false

        // Setup Spinner
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Knockout", "League")
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter

        radioGroupMode.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radio_doubles) {
                inputName2.visibility = android.view.View.VISIBLE
                inputName.hint = "Player 1 Name"
            } else {
                inputName2.visibility = android.view.View.GONE
                inputName.hint = "Player Name"
            }
        }

        btnAdd.setOnClickListener {
            var name = inputName.text.toString().trim()
            val name2 = inputName2.text.toString().trim()
            val isDoubles = radioGroupMode.checkedRadioButtonId == R.id.radio_doubles

            if (isDoubles) {
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

            val selectedType = spinnerType.selectedItem as String
            val bracketText = if (selectedType == "League") {
                tournamentManager.generateLeagueText(participants)
            } else {
                tournamentManager.generateBracketText(participants, topSeed)
            }

            txtBracket.setText(bracketText)
        }

        btnPublish.setOnClickListener {
            val bracketText = txtBracket.text.toString()
            if (bracketText.isEmpty() || bracketText.contains("Add players")) {
                Toast.makeText(this, "Generate fixtures first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tName = inputTournamentName.text.toString().ifEmpty { "Tournament" }
            val tournament = Tournament(
                id = currentTournamentId,
                name = tName,
                participants = participants,
                bracketText = bracketText,
                isPublic = checkPublic.isChecked,
                imageUrl = selectedImageUri?.toString() ?: ""
            )

            lifecycleScope.launch {
                val result = tournamentRepository.saveTournament(tournament)
                if (result.isSuccess) {
                    currentTournamentId = result.getOrNull() ?: currentTournamentId
                    Toast.makeText(this@TournamentActivity, "Tournament Saved/Published!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@TournamentActivity, "Failed to save tournament", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnViewPublic.setOnClickListener {
             startActivity(Intent(this, PublicTournamentsActivity::class.java))
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

        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE)
        }

        btnEdit.setOnClickListener {
            txtBracket.focusable = android.view.View.FOCUSABLE
            txtBracket.isFocusableInTouchMode = true
            txtBracket.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(txtBracket, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
            Toast.makeText(this, "Editing Enabled", Toast.LENGTH_SHORT).show()
        }

        btnImport.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*" // Allow all to ensure CSV/Text are picked up easily
            val mimeTypes = arrayOf("text/csv", "text/comma-separated-values", "text/plain")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            startActivityForResult(intent, REQUEST_IMPORT)
        }

        btnDownload.setOnClickListener {
            if (participants.isEmpty()) {
                Toast.makeText(this, "No participants to download", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/csv"
                putExtra(Intent.EXTRA_TITLE, "participants.csv")
            }
            startActivityForResult(intent, REQUEST_CREATE_FILE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_IMAGE -> {
                    selectedImageUri = data.data
                    val statusText = findViewById<TextView>(R.id.txt_image_status)
                    val imgPreview = findViewById<android.widget.ImageView>(R.id.img_tournament_preview)

                    statusText.text = "Image Selected"
                    imgPreview.visibility = android.view.View.VISIBLE
                    imgPreview.setImageURI(selectedImageUri)

                    // Persist permission so we can read it later
                    selectedImageUri?.let { uri ->
                        try {
                            contentResolver.takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        } catch (e: Exception) {
                            // Ignored if not possible
                        }
                    }
                }
                REQUEST_IMPORT -> {
                    data.data?.let { uri ->
                        try {
                            contentResolver.openInputStream(uri)?.use { inputStream ->
                                val reader = java.io.BufferedReader(java.io.InputStreamReader(inputStream))
                                var line = reader.readLine()
                                var count = 0
                                while (line != null) {
                                    val name = line.trim().replace(",", "") // Simple cleanup
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

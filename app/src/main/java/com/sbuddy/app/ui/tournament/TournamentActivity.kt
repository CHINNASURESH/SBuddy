package com.sbuddy.app.ui.tournament

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.Manifest
import android.content.pm.PackageManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.sbuddy.app.BaseActivity
import com.sbuddy.app.R
import com.sbuddy.app.data.model.Tournament
import com.sbuddy.app.data.repository.AuthRepository
import com.sbuddy.app.data.repository.TournamentRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch

class TournamentActivity : BaseActivity() {

    private val tournamentRepository = TournamentRepository()
    private val authRepository = AuthRepository()
    private var selectedImageUri: android.net.Uri? = null
    private var currentTournamentId: String = ""

    // Loaded State
    private var currentCreatorId: String = ""
    private var currentCreatedDate: Long = 0
    private var currentImageUrl: String = ""
    private var currentStatus: String = "Open"

    // We need to keep participants/rounds if we load an existing tournament,
    // to avoid overwriting them with empty lists when saving basic info!
    private var currentParticipants = listOf<String>()
    private var currentRounds = listOf<com.sbuddy.app.data.model.Match>()
    private var currentBracketText = ""

    companion object {
        private const val REQUEST_IMAGE = 201
        private const val REQUEST_PERMISSION_STORAGE = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournament)

        // Views
        val spinnerType = findViewById<Spinner>(R.id.spinner_tournament_type)
        val btnSelectImage = findViewById<Button>(R.id.btn_select_image)
        val btnNext = findViewById<Button>(R.id.btn_next)
        val btnViewPublic = findViewById<Button>(R.id.btn_view_public)

        // Load existing if ID passed
        val intentId = intent.getStringExtra("TOURNAMENT_ID")
        if (!intentId.isNullOrEmpty()) {
            loadTournament(intentId)
        }

        // Check Auth
        if (authRepository.getCurrentUser() == null) {
            Toast.makeText(this, "You must be logged in to save tournaments.", Toast.LENGTH_LONG).show()
        }

        // Setup Spinner
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Knockout", "League")
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter

        btnSelectImage.setOnClickListener {
            if (checkStoragePermission()) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "image/*"
                startActivityForResult(intent, REQUEST_IMAGE)
            }
        }

        btnNext.setOnClickListener {
             saveAndNavigate()
        }

        btnViewPublic.setOnClickListener {
             startActivity(Intent(this, PublicTournamentsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        if (currentTournamentId.isNotEmpty()) {
            loadTournament(currentTournamentId)
        }
    }

    private fun loadTournament(id: String) {
        lifecycleScope.launch {
            val result = tournamentRepository.getTournament(id)
            if (result.isSuccess) {
                val tournament = result.getOrNull()
                if (tournament != null) {
                    currentTournamentId = tournament.id
                    currentCreatorId = tournament.creatorId
                    currentCreatedDate = tournament.date
                    currentImageUrl = tournament.imageUrl
                    currentStatus = tournament.status
                    currentParticipants = tournament.participants
                    currentRounds = tournament.rounds
                    currentBracketText = tournament.bracketText

                    findViewById<EditText>(R.id.input_tournament_name).setText(tournament.name)
                    findViewById<EditText>(R.id.input_tournament_location).setText(tournament.location)
                    findViewById<EditText>(R.id.input_tournament_category).setText(tournament.category)
                    findViewById<EditText>(R.id.input_court_name).setText(tournament.courtName)
                    findViewById<EditText>(R.id.input_organizer_mobile).setText(tournament.organizerMobile)
                    findViewById<CheckBox>(R.id.check_public).isChecked = tournament.isPublic

                    // Set Spinner
                    val spinnerType = findViewById<Spinner>(R.id.spinner_tournament_type)
                    val adapter = spinnerType.adapter as ArrayAdapter<String>
                    val pos = adapter.getPosition(tournament.type)
                    if (pos >= 0) spinnerType.setSelection(pos)

                    // Set Mode
                    if (tournament.mode == "Doubles") {
                        findViewById<RadioButton>(R.id.radio_doubles).isChecked = true
                    } else {
                        findViewById<RadioButton>(R.id.radio_singles).isChecked = true
                    }

                    if (tournament.imageUrl.isNotEmpty()) {
                        selectedImageUri = android.net.Uri.parse(tournament.imageUrl)
                        findViewById<android.widget.ImageView>(R.id.img_tournament_preview).visibility = View.VISIBLE
                        findViewById<android.widget.ImageView>(R.id.img_tournament_preview).setImageURI(selectedImageUri)
                    }

                    findViewById<Button>(R.id.btn_next).text = "Next: Participants & Fixtures"
                } else {
                    Toast.makeText(this@TournamentActivity, "Tournament not found", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@TournamentActivity, "Error loading tournament", Toast.LENGTH_SHORT).show()
            }
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

    private fun saveAndNavigate() {
        val inputTournamentName = findViewById<EditText>(R.id.input_tournament_name)
        val inputLocation = findViewById<EditText>(R.id.input_tournament_location)
        val inputCategory = findViewById<EditText>(R.id.input_tournament_category)
        val inputCourtName = findViewById<EditText>(R.id.input_court_name)
        val inputOrganizerMobile = findViewById<EditText>(R.id.input_organizer_mobile)
        val checkPublic = findViewById<CheckBox>(R.id.check_public)
        val progressBar = findViewById<android.widget.ProgressBar>(R.id.progress_bar)
        val btnNext = findViewById<Button>(R.id.btn_next)

        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to save tournaments.", Toast.LENGTH_LONG).show()
            return
        }
        val currentUserId = currentUser.uid

        progressBar.visibility = View.VISIBLE
        btnNext.isEnabled = false

        val tName = inputTournamentName.text.toString().ifEmpty { "Tournament" }
        val tLocation = inputLocation.text.toString()
        val tCategory = inputCategory.text.toString()
        val tCourt = inputCourtName.text.toString()
        val tMobile = inputOrganizerMobile.text.toString()

        val spinnerType = findViewById<Spinner>(R.id.spinner_tournament_type)
        val radioGroupMode = findViewById<RadioGroup>(R.id.radio_group_mode)

        val tType = spinnerType.selectedItem.toString()
        val tMode = if (radioGroupMode.checkedRadioButtonId == R.id.radio_doubles) "Doubles" else "Singles"

        lifecycleScope.launch {
            // Fetch latest data if editing to preserve participants/rounds
            var baseTournament: Tournament? = null
            if (currentTournamentId.isNotEmpty()) {
                val fetch = tournamentRepository.getTournament(currentTournamentId)
                baseTournament = fetch.getOrNull()
            }

            val tournament = if (baseTournament != null) {
                baseTournament.copy(
                    name = tName,
                    location = tLocation,
                    category = tCategory,
                    courtName = tCourt,
                    organizerMobile = tMobile,
                    type = tType,
                    mode = tMode,
                    isPublic = checkPublic.isChecked,
                    imageUrl = selectedImageUri?.toString() ?: currentImageUrl
                    // Preserve others from baseTournament
                )
            } else {
                Tournament(
                    id = currentTournamentId,
                    name = tName,
                    creatorId = if (currentCreatorId.isNotEmpty()) currentCreatorId else currentUserId,
                    date = if (currentCreatedDate > 0) currentCreatedDate else System.currentTimeMillis(),
                    organizerMobile = tMobile,
                    courtName = tCourt,
                    category = tCategory,
                    type = tType,
                    mode = tMode,
                    participants = currentParticipants, // Preserve existing
                    bracketText = currentBracketText, // Preserve existing
                    rounds = currentRounds, // Preserve existing
                    isPublic = checkPublic.isChecked,
                    imageUrl = selectedImageUri?.toString() ?: currentImageUrl,
                    location = tLocation,
                    status = currentStatus
                )
            }

            var result = tournamentRepository.saveTournament(tournament)

            // Verification
            if (result.isFailure && result.exceptionOrNull() is TimeoutCancellationException) {
                if (currentTournamentId.isNotEmpty()) {
                    val check = tournamentRepository.getTournament(currentTournamentId)
                    if (check.isSuccess && check.getOrNull()?.name == tName) {
                        result = Result.success(currentTournamentId)
                    }
                }
            }

            progressBar.visibility = View.GONE
            btnNext.isEnabled = true

            if (result.isSuccess) {
                currentTournamentId = result.getOrNull() ?: currentTournamentId
                Toast.makeText(this@TournamentActivity, "Basic Info Saved!", Toast.LENGTH_SHORT).show()

                // Navigate
                val intent = Intent(this@TournamentActivity, TournamentFixturesActivity::class.java)
                intent.putExtra("TOURNAMENT_ID", currentTournamentId)
                startActivity(intent)
            } else {
                val ex = result.exceptionOrNull()
                if (ex is TimeoutCancellationException || ex?.message?.contains("Timed out") == true) {
                    Toast.makeText(this@TournamentActivity, "Failed to save. Please try again.", Toast.LENGTH_LONG).show()
                } else {
                    val msg = ex?.message ?: "Unknown error"
                    Toast.makeText(this@TournamentActivity, "Failed to save: $msg", Toast.LENGTH_LONG).show()
                }
            }
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
                    imgPreview.visibility = View.VISIBLE
                    imgPreview.setImageURI(selectedImageUri)

                    selectedImageUri?.let { uri ->
                        try {
                            contentResolver.takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        } catch (e: Exception) {
                            // Ignored
                        }
                    }
                }
            }
        }
    }
}

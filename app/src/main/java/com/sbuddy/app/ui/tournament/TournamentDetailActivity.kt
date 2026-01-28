package com.sbuddy.app.ui.tournament

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sbuddy.app.BaseActivity
import com.sbuddy.app.R
import com.sbuddy.app.data.repository.AuthRepository
import com.sbuddy.app.data.repository.TournamentRepository
import kotlinx.coroutines.launch
import android.content.Intent

class TournamentDetailActivity : BaseActivity() {

    private val repository = TournamentRepository()
    private val authRepository = AuthRepository()
    private lateinit var adapter: FixtureAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournament_detail)

        val tournamentId = intent.getStringExtra("TOURNAMENT_ID")
        if (tournamentId.isNullOrEmpty()) {
            Toast.makeText(this, "Tournament not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val txtTitle = findViewById<TextView>(R.id.txt_toolbar_title)
        val imgHeader = findViewById<ImageView>(R.id.img_header)
        val recycler = findViewById<RecyclerView>(R.id.recycler_detail_fixtures)
        val btnManage = findViewById<View>(R.id.btn_manage_tournament)

        recycler.layoutManager = LinearLayoutManager(this)
        // Read-only adapter
        adapter = FixtureAdapter(isReadOnly = true) {
            // No action on click
        }
        recycler.adapter = adapter

        lifecycleScope.launch {
            val result = repository.getTournament(tournamentId)
            if (result.isSuccess) {
                val tournament = result.getOrNull()
                if (tournament != null) {
                    txtTitle.text = tournament.name
                    if (tournament.imageUrl.isNotEmpty()) {
                        try {
                            imgHeader.visibility = View.VISIBLE
                            imgHeader.setImageURI(Uri.parse(tournament.imageUrl))
                        } catch (e: Exception) {
                            imgHeader.visibility = View.GONE
                        }
                    }

                    // Check ownership
                    val currentUser = authRepository.getCurrentUser()
                    if (currentUser != null && currentUser.uid == tournament.creatorId) {
                        btnManage.visibility = View.VISIBLE
                        btnManage.setOnClickListener {
                            val intent = Intent(this@TournamentDetailActivity, TournamentActivity::class.java)
                            intent.putExtra("TOURNAMENT_ID", tournament.id)
                            startActivity(intent)
                        }
                    }

                    if (tournament.rounds.isNotEmpty()) {
                         adapter.setMatches(tournament.rounds)
                    } else {
                         Toast.makeText(this@TournamentDetailActivity, "No interactive fixtures found.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@TournamentDetailActivity, "Tournament not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                Toast.makeText(this@TournamentDetailActivity, "Error loading tournament", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

package com.sbuddy.app.ui.tournament

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sbuddy.app.BaseActivity
import com.sbuddy.app.R
import com.sbuddy.app.data.model.Tournament
import com.sbuddy.app.data.repository.TournamentRepository
import kotlinx.coroutines.launch

class PublicTournamentsActivity : BaseActivity() {

    private val tournamentRepository = TournamentRepository()
    private lateinit var adapter: TournamentAdapter
    private val REQUEST_PERMISSIONS = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_public_tournaments)

        checkPermissions()

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_tournaments)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TournamentAdapter { tournament ->
            val intent = android.content.Intent(this, TournamentDetailActivity::class.java)
            intent.putExtra("TOURNAMENT_ID", tournament.id)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        loadTournaments()

        findViewById<View>(R.id.fab_create_tournament).setOnClickListener {
            startActivity(android.content.Intent(this, TournamentActivity::class.java))
        }
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), REQUEST_PERMISSIONS)
        }
    }

    private fun loadTournaments() {
        lifecycleScope.launch {
            val result = tournamentRepository.getPublicTournaments()
            if (result.isSuccess) {
                val tournaments = result.getOrNull() ?: emptyList()
                adapter.setTournaments(tournaments)
                if (tournaments.isEmpty()) {
                    Toast.makeText(this@PublicTournamentsActivity, "No public tournaments found", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@PublicTournamentsActivity, "Error loading tournaments: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadTournaments()
    }

}

class TournamentAdapter(private val onItemClick: (Tournament) -> Unit) : RecyclerView.Adapter<TournamentAdapter.ViewHolder>() {

    private val tournaments = mutableListOf<Tournament>()

    fun setTournaments(list: List<Tournament>) {
        tournaments.clear()
        tournaments.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tournament, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(tournaments[position], onItemClick)
    }

    override fun getItemCount() = tournaments.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameView: TextView = itemView.findViewById(R.id.text_tournament_name)
        private val countView: TextView = itemView.findViewById(R.id.text_participants_count)
        private val locView: TextView = itemView.findViewById(R.id.text_tournament_location)

        fun bind(tournament: Tournament, onItemClick: (Tournament) -> Unit) {
            nameView.text = tournament.name
            countView.text = "Participants: ${tournament.participants.size}"

            if (tournament.location.isNotEmpty()) {
                locView.visibility = View.VISIBLE
                locView.text = "📍 ${tournament.location}"
            } else {
                locView.visibility = View.GONE
            }

            itemView.setOnClickListener { onItemClick(tournament) }
        }
    }
}

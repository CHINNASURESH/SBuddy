package com.sbuddy.app.ui.tournament

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_public_tournaments)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_tournaments)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TournamentAdapter { tournament ->
            val intent = android.content.Intent(this, TournamentDetailActivity::class.java)
            intent.putExtra("TOURNAMENT_ID", tournament.id)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            val result = tournamentRepository.getPublicTournaments()
            if (result.isSuccess) {
                val tournaments = result.getOrNull() ?: emptyList()
                adapter.setTournaments(tournaments)
            }
        }
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

        fun bind(tournament: Tournament, onItemClick: (Tournament) -> Unit) {
            nameView.text = tournament.name
            countView.text = "Participants: ${tournament.participants.size}"
            itemView.setOnClickListener { onItemClick(tournament) }
        }
    }
}

package com.sbuddy.app.ui.history

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.sbuddy.app.R
import com.sbuddy.app.data.model.Match
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MatchHistoryActivity : AppCompatActivity() {

    private val allMatches = listOf(
        Match("1", "Player 1 & Player 2", "Player 3 & Player 4", 21, 16, System.currentTimeMillis(), "Player 1 & Player 2"),
        Match("2", "Chloe & Dave", "Eve & Frank", 19, 21, System.currentTimeMillis() - 86400000, "Eve & Frank"),
        Match("3", "Alex & Grace", "Ben & Heidi", 21, 19, System.currentTimeMillis() - 172800000, "Alex & Grace")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_history)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_history)
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)

        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = MatchHistoryAdapter(allMatches)
        recyclerView.adapter = adapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Filter logic would go here. For now just shuffling/reloading to simulate change
                // In real app, we filter by Singles/Doubles if we had that flag in Match model
                adapter.updateList(allMatches.reversed())
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
}

class MatchHistoryAdapter(private var matches: List<Match>) : RecyclerView.Adapter<MatchHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.txt_date)
        val team1: TextView = view.findViewById(R.id.txt_team1)
        val team2: TextView = view.findViewById(R.id.txt_team2)
        val score: TextView = view.findViewById(R.id.txt_score)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_match_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val match = matches[position]
        holder.team1.text = match.player1Name
        holder.team2.text = match.player2Name
        holder.score.text = "${match.player1Score}-${match.player2Score}"

        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        holder.date.text = sdf.format(Date(match.timestamp))
    }

        val matches = MatchRepository.getMatches()
        val displayList = matches.map { match ->
            val type = if (match.isDoubles) "Doubles" else "Singles"
            "[$type] ${match.player1Name} vs ${match.player2Name}\nScore: ${match.player1Score}-${match.player2Score}\nWinner: ${match.winner}"
        }

    fun updateList(newMatches: List<Match>) {
        matches = newMatches
        notifyDataSetChanged()
    }
}

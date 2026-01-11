package com.sbuddy.app.ui.history

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.sbuddy.app.BaseActivity
import com.sbuddy.app.R
import com.sbuddy.app.data.model.Match
import com.sbuddy.app.data.repository.MatchRepository
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MatchHistoryActivity : BaseActivity() {

    private lateinit var repository: MatchRepository
    private var allMatches: List<Match> = emptyList()
    private var currentTab = 0 // 0 = Singles, 1 = Doubles

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_history)

        repository = MatchRepository(applicationContext)

        val userName = intent.getStringExtra("USER_NAME")
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_history)
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)

        val adapter = MatchHistoryAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        repository.getHistory { matches ->
            // Filter by user if needed (Buddy feature)
            var filtered = matches
            if (!userName.isNullOrEmpty()) {
                 filtered = matches.filter {
                     it.player1Name.contains(userName, true) || it.player2Name.contains(userName, true)
                 }
            }
            allMatches = filtered
            updateList(adapter)
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                updateList(adapter)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun updateList(adapter: MatchHistoryAdapter) {
        val filtered = if (currentTab == 0) {
            allMatches.filter { it.isSingles }
        } else {
            allMatches.filter { !it.isSingles }
        }
        adapter.updateList(filtered)
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

    override fun getItemCount() = matches.size

    fun updateList(newMatches: List<Match>) {
        matches = newMatches
        notifyDataSetChanged()
    }
}

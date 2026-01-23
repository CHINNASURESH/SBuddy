package com.sbuddy.app.ui.tournament

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sbuddy.app.R
import com.sbuddy.app.data.model.Match

class FixtureAdapter(
    private val isReadOnly: Boolean = false,
    private val onScoreClick: (Match) -> Unit
) : RecyclerView.Adapter<FixtureAdapter.FixtureViewHolder>() {

    private val matches = mutableListOf<Match>()

    fun setMatches(list: List<Match>) {
        matches.clear()
        matches.addAll(list)
        notifyDataSetChanged()
    }

    fun getMatches(): List<Match> = matches

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FixtureViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fixture, parent, false)
        return FixtureViewHolder(view)
    }

    override fun onBindViewHolder(holder: FixtureViewHolder, position: Int) {
        holder.bind(matches[position], onScoreClick, isReadOnly)
    }

    override fun getItemCount() = matches.size

    class FixtureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtLabel: TextView = itemView.findViewById(R.id.txt_match_label)
        private val txtP1: TextView = itemView.findViewById(R.id.txt_player1)
        private val txtP2: TextView = itemView.findViewById(R.id.txt_player2)
        private val txtS1: TextView = itemView.findViewById(R.id.txt_score_p1)
        private val txtS2: TextView = itemView.findViewById(R.id.txt_score_p2)
        private val btnScore: Button = itemView.findViewById(R.id.btn_score)
        private val txtWinner: TextView = itemView.findViewById(R.id.txt_winner)

        fun bind(match: Match, onScoreClick: (Match) -> Unit, isReadOnly: Boolean) {
            txtLabel.text = match.matchLabel
            txtP1.text = match.player1Name
            txtP2.text = match.player2Name
            txtS1.text = match.player1Score.toString()
            txtS2.text = match.player2Score.toString()

            if (match.winner != null) {
                txtWinner.visibility = View.VISIBLE
                txtWinner.text = "Winner: ${match.winner}"
                btnScore.visibility = View.GONE
            } else {
                txtWinner.visibility = View.GONE
                btnScore.visibility = if (isReadOnly) View.GONE else View.VISIBLE
            }

            // Disable scoring if players are placeholders
            val isPlayable = !match.player1Name.startsWith("Winner ") && !match.player2Name.startsWith("Winner ")
            btnScore.isEnabled = isPlayable

            btnScore.setOnClickListener {
                if (isPlayable) onScoreClick(match)
            }
        }
    }
}

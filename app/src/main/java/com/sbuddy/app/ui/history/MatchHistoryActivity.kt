package com.sbuddy.app.ui.history

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.sbuddy.app.data.repository.MatchRepository

class MatchHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Creating a simple layout programmatically for simplicity, or I should create XML.
        // I'll create a simple ListView programmatically.
        val listView = ListView(this)
        setContentView(listView)

        val matches = MatchRepository.getMatches()
        val displayList = matches.map { match ->
            val type = if (match.isDoubles) "Doubles" else "Singles"
            "[$type] ${match.player1Name} vs ${match.player2Name}\nScore: ${match.player1Score}-${match.player2Score}\nWinner: ${match.winner}"
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayList)
        listView.adapter = adapter
    }
}

package com.sbuddy.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sbuddy.app.data.model.Match

class MatchRepository {

    // In-memory storage for demo purposes
    companion object {
        private val matches = mutableListOf(
            Match("1", "Player 1 & Player 2", "Player 3 & Player 4", 21, 16, System.currentTimeMillis(), "Player 1 & Player 2", false),
            Match("2", "Chloe & Dave", "Eve & Frank", 19, 21, System.currentTimeMillis() - 86400000, "Eve & Frank", false),
            Match("3", "Alex & Grace", "Ben & Heidi", 21, 19, System.currentTimeMillis() - 172800000, "Alex & Grace", false),
            Match("4", "Alice", "Bob", 21, 15, System.currentTimeMillis() - 200000000, "Alice", true),
            Match("5", "Alice & Charlie", "Dave & Bob", 21, 18, System.currentTimeMillis() - 250000000, "Alice & Charlie", false)
        )
    }

    fun saveMatch(match: Match) {
        matches.add(0, match) // Add to top
    }

    fun getHistory(callback: (List<Match>) -> Unit) {
        callback(matches)
    }
}

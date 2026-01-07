package com.sbuddy.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sbuddy.app.data.model.Match

class MatchRepository {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun saveMatch(match: Match) {
        // Placeholder for future implementation
    }

    fun getHistory(userId: String, callback: (List<Match>) -> Unit) {
        // Placeholder for future implementation
        callback(emptyList())
    }
}

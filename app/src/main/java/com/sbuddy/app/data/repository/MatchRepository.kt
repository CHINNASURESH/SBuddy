package com.sbuddy.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sbuddy.app.data.model.Match

class MatchRepository {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun saveMatch(match: Match) {
        // In a real app, this would be:
        // db.collection("matches").add(match)
        // For now, we just define the method structure
    }

    fun getHistory(userId: String, callback: (List<Match>) -> Unit) {
        // db.collection("matches").whereEqualTo("userId", userId)...
        callback(emptyList())
    }
}

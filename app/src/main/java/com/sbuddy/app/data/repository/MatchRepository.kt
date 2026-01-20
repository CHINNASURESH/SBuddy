package com.sbuddy.app.data.repository

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sbuddy.app.data.model.Match

class MatchRepository(private val context: Context) {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val PREFS_NAME = "sbuddy_prefs"
    private val KEY_MATCH_HISTORY = "match_history"
    private val gson = Gson()

    companion object {
        // Fallback in-memory list if context fails or first run
        private var matches = mutableListOf(
            Match("1", "Player 1 & Player 2", "Player 3 & Player 4", 21, 16, System.currentTimeMillis(), "Player 1 & Player 2", false),
            Match("2", "Chloe & Dave", "Eve & Frank", 19, 21, System.currentTimeMillis() - 86400000, "Eve & Frank", false),
            Match("3", "Alex & Grace", "Ben & Heidi", 21, 19, System.currentTimeMillis() - 172800000, "Alex & Grace", false),
            Match("4", "Alice", "Bob", 21, 15, System.currentTimeMillis() - 200000000, "Alice", true),
            Match("5", "Alice & Charlie", "Dave & Bob", 21, 18, System.currentTimeMillis() - 250000000, "Alice & Charlie", false)
        )
        private var isInitialized = false
    }

    init {
        if (!isInitialized) {
            loadMatches()
            isInitialized = true
        }
    }

    private fun loadMatches() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_MATCH_HISTORY, null)
        if (json != null) {
            val type = object : TypeToken<MutableList<Match>>() {}.type
            val savedMatches: MutableList<Match> = gson.fromJson(json, type)
            // Merge with default/mock data if needed, or strictly use saved data.
            // For this app, let's prioritize saved data but keep mock if saved is empty to show something.
            if (savedMatches.isNotEmpty()) {
                matches = savedMatches
            }
        }
    }

    private fun saveMatches() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val json = gson.toJson(matches)
        editor.putString(KEY_MATCH_HISTORY, json)
        editor.apply()
    }

    fun saveMatch(match: Match) {
        matches.add(0, match) // Add to top
        saveMatches()

        db.collection("matches").add(match)
    }

    fun getHistory(callback: (List<Match>) -> Unit) {
        db.collection("matches")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val matchList = result.toObjects(Match::class.java)
                callback(matchList)
            }
            .addOnFailureListener {
                // Fallback to local history
                callback(matches)
            }
    }
}

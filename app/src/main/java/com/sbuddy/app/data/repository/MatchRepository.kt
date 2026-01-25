package com.sbuddy.app.data.repository

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sbuddy.app.data.model.Match
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class MatchRepository(private val context: Context) {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val PREFS_NAME = "sbuddy_prefs"
    private val KEY_MATCH_HISTORY = "match_history"
    private val gson = Gson()

    val isMockMode: Boolean by lazy {
        try {
            FirebaseApp.getInstance().options.projectId == "mock-project-id"
        } catch (e: Exception) {
            false
        }
    }

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

    suspend fun saveMatch(match: Match): Result<Unit> {
        // Always save locally first as backup/cache
        matches.add(0, match) // Add to top
        saveMatches()

        if (isMockMode) {
            return Result.success(Unit)
        }

        return try {
            withTimeout(10000L) {
                db.collection("matches").add(match).await()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            // Log error but treat as success from UI perspective since we saved locally?
            // Or return failure to let UI know cloud sync failed.
            // For now, return failure but UI should know local save worked.
            Result.failure(e)
        }
    }

    suspend fun getHistory(): Result<List<Match>> {
        if (isMockMode) {
            return Result.success(matches)
        }

        return try {
            withTimeout(10000L) {
                val result = db.collection("matches")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()
                val matchList = result.toObjects(Match::class.java)

                // Update local cache if cloud fetch succeeds
                if (matchList.isNotEmpty()) {
                    matches = matchList.toMutableList()
                    saveMatches()
                }

                Result.success(matchList)
            }
        } catch (e: Exception) {
            // Fallback to local history
            Result.success(matches)
        }
    }
}

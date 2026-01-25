package com.sbuddy.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sbuddy.app.data.model.Tournament
import kotlinx.coroutines.tasks.await
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.withTimeout

class TournamentRepository {
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val collection = firestore.collection("tournaments")

    // Check if we are running with the mock google-services.json
    val isMockMode: Boolean by lazy {
        try {
            FirebaseApp.getInstance().options.projectId == "mock-project-id"
        } catch (e: Exception) {
            false
        }
    }

    suspend fun saveTournament(tournament: Tournament): Result<String> {
        if (isMockMode) {
            // In mock mode, we just return a success result with a fake ID.
            // Ideally we would save to local prefs like MatchRepository, but for now we just avoid the crash.
            return Result.success("mock-tournament-id-" + System.currentTimeMillis())
        }
        return try {
            withTimeout(15000L) {
                // If ID exists, update. If not, create new.
                // Using set() with merge is safer for updates, but since we overwrite the whole object here:
                val docRef = if (tournament.id.isEmpty()) {
                    collection.document()
                } else {
                    collection.document(tournament.id)
                }

                // Ensure the tournament object has the correct ID before saving
                val tournamentToSave = tournament.copy(id = docRef.id)

                // Use set (overwrite)
                docRef.set(tournamentToSave).await()
                Result.success(docRef.id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPublicTournaments(): Result<List<Tournament>> {
        if (isMockMode) {
             val mockData = listOf(
                Tournament(id = "mock1", name = "Mock Tournament 1", isPublic = true, location = "New York", participants = mutableListOf("A", "B")),
                Tournament(id = "mock2", name = "Mock Tournament 2", isPublic = true, location = "London", participants = mutableListOf("C", "D"))
            )
            return Result.success(mockData)
        }

        return try {
            withTimeout(15000L) {
                val snapshot = collection.whereEqualTo("isPublic", true).get().await()
                var tournaments = snapshot.toObjects(Tournament::class.java)

                // Simple sort: Put tournaments with location at the top (simulating 'nearby' priority if user has location)
                // Ideally, we would sort by distance from user. For now, we prioritize those that HAVE a location.
                tournaments = tournaments.sortedByDescending { it.location.isNotEmpty() }

                Result.success(tournaments)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTournament(id: String): Result<Tournament?> {
        if (isMockMode) {
            if (id.startsWith("mock")) {
                return Result.success(Tournament(id = id, name = "Mock Tournament", isPublic = true, location = "Mock City"))
            }
            return Result.success(null)
        }

        return try {
            withTimeout(10000L) {
                val doc = collection.document(id).get().await()
                if (doc.exists()) {
                    Result.success(doc.toObject(Tournament::class.java))
                } else {
                    Result.success(null)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

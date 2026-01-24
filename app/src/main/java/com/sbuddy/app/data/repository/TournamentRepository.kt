package com.sbuddy.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sbuddy.app.data.model.Tournament
import kotlinx.coroutines.tasks.await
import com.google.firebase.FirebaseApp

class TournamentRepository {
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val collection = firestore.collection("tournaments")

    val isMockMode: Boolean by lazy {
        try {
            FirebaseApp.getInstance().options.projectId == "mock-project-id"
        } catch (e: Exception) {
            false
        }
    }

    suspend fun saveTournament(tournament: Tournament): Result<String> {
        if (isMockMode) {
            return Result.success("mock-tournament-id")
        }
        return try {
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
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPublicTournaments(): Result<List<Tournament>> {
        if (isMockMode) {
            // Return some mock data to verify UI
             val mockData = listOf(
                Tournament(id = "mock1", name = "Mock Tournament 1", isPublic = true, location = "New York"),
                Tournament(id = "mock2", name = "Mock Tournament 2", isPublic = true, location = "London")
            )
            return Result.success(mockData)
        }
        return try {
            val snapshot = collection.whereEqualTo("isPublic", true).get().await()
            var tournaments = snapshot.toObjects(Tournament::class.java)

            // Simple sort: Put tournaments with location at the top (simulating 'nearby' priority if user has location)
            // Ideally, we would sort by distance from user. For now, we prioritize those that HAVE a location.
            tournaments = tournaments.sortedByDescending { it.location.isNotEmpty() }

            Result.success(tournaments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTournament(id: String): Result<Tournament?> {
        if (isMockMode) {
            if (id == "mock1") {
                return Result.success(Tournament(id = "mock1", name = "Mock Tournament 1", isPublic = true, location = "New York"))
            } else if (id == "mock2") {
                return Result.success(Tournament(id = "mock2", name = "Mock Tournament 2", isPublic = true, location = "London"))
            }
            return Result.success(null)
        }
        return try {
            val doc = collection.document(id).get().await()
            if (doc.exists()) {
                Result.success(doc.toObject(Tournament::class.java))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

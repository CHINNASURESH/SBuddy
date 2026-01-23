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
            return Result.success(emptyList())
        }
        return try {
            val snapshot = collection.whereEqualTo("isPublic", true).get().await()
            val tournaments = snapshot.toObjects(Tournament::class.java)
            Result.success(tournaments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTournament(id: String): Result<Tournament?> {
        if (isMockMode) {
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

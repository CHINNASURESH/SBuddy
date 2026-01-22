package com.sbuddy.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sbuddy.app.data.model.Tournament
import kotlinx.coroutines.tasks.await

class TournamentRepository {
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val collection = firestore.collection("tournaments")

    suspend fun saveTournament(tournament: Tournament): Result<Unit> {
        return try {
            val docRef = if (tournament.id.isEmpty()) {
                collection.document()
            } else {
                collection.document(tournament.id)
            }
            val tournamentToSave = tournament.copy(id = docRef.id)
            docRef.set(tournamentToSave).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPublicTournaments(): Result<List<Tournament>> {
        return try {
            val snapshot = collection.whereEqualTo("isPublic", true).get().await()
            val tournaments = snapshot.toObjects(Tournament::class.java)
            Result.success(tournaments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

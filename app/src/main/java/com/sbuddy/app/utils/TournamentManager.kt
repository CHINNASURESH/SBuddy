package com.sbuddy.app.utils

import com.sbuddy.app.data.model.Match

class TournamentManager {

    /**
     * Generates a single-elimination fixture list.
     * Takes a list of participant names and returns a structured string representation of the full bracket.
     */
    fun generateBracketText(participants: List<String>, topSeed: String? = null): String {
        if (participants.size < 2) return "Need at least 2 participants."

        // Handle seeding: move topSeed to index 0
        val workingList = participants.toMutableList()
        if (topSeed != null && workingList.contains(topSeed)) {
            workingList.remove(topSeed)
            workingList.shuffle()
            workingList.add(0, topSeed)
        } else {
            workingList.shuffle()
        }

        val sb = StringBuilder()
        var round = 1
        var currentRoundParticipants = workingList.toList()

        while (currentRoundParticipants.size > 1) {
            sb.append("=== ROUND $round ===\n\n")

            val nextRoundParticipants = mutableListOf<String>()
            val matchCount = (currentRoundParticipants.size + 1) / 2

            for (i in 0 until matchCount) {
                val p1Index = i * 2
                val p2Index = i * 2 + 1

                if (p2Index < currentRoundParticipants.size) {
                    val p1 = currentRoundParticipants[p1Index]
                    val p2 = currentRoundParticipants[p2Index]
                    sb.append("Match ${i + 1}: $p1 vs $p2\n")
                    nextRoundParticipants.add("Winner M${i+1}")
                } else {
                    // Bye
                    val p1 = currentRoundParticipants[p1Index]
                    sb.append("Match ${i + 1}: $p1 vs BYE\n")
                    nextRoundParticipants.add(p1) // Moves to next round automatically
                }
            }
            sb.append("\n")
            currentRoundParticipants = nextRoundParticipants
            round++
        }

        sb.append("=== WINNER ===\n")
        sb.append(currentRoundParticipants.firstOrNull() ?: "TBD")

        return sb.toString()
    }

    // Kept for backward compatibility if needed, but updated logic is in generateBracketText
    fun generateFixtures(participants: List<String>): List<Match> {
        // ... (previous simple logic)
        return emptyList()
    }
}

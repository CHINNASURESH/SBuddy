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
        var matchGlobalCount = 1
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
                    sb.append("Match $matchGlobalCount: $p1 vs $p2\n")
                    nextRoundParticipants.add("Winner M$matchGlobalCount")
                    matchGlobalCount++
                } else {
                    // Bye
                    val p1 = currentRoundParticipants[p1Index]
                    sb.append("Bye: $p1 (Advances)\n")
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

    /**
     * Generates a Round Robin fixture list.
     * Each participant plays every other participant.
     */
    fun generateRoundRobinText(participants: List<String>): String {
        if (participants.size < 2) return "Need at least 2 participants."

        val sb = StringBuilder()
        val players = participants.toMutableList()

        // If odd number of players, add a dummy "Bye" player
        if (players.size % 2 != 0) {
            players.add("BYE")
        }

        val numRounds = players.size - 1
        val halfSize = players.size / 2
        var matchGlobalCount = 1

        val teamSize = players.size

        for (round in 0 until numRounds) {
            sb.append("=== ROUND ${round + 1} ===\n\n")

            for (i in 0 until halfSize) {
                val p1 = players[i]
                val p2 = players[teamSize - 1 - i]

                if (p1 == "BYE" || p2 == "BYE") {
                    val realPlayer = if (p1 == "BYE") p2 else p1
                    sb.append("$realPlayer has a BYE\n")
                } else {
                    sb.append("Match $matchGlobalCount: $p1 vs $p2\n")
                    matchGlobalCount++
                }
            }
            sb.append("\n")

            // Rotate players clockwise, keeping the first player fixed
            // Indices: 0, 1, 2, 3 ... N-1
            // Fixed: 0
            // Moving: 1 -> 2 -> ... -> N-1 -> 1

            val last = players.removeAt(players.size - 1)
            players.add(1, last)
        }

        return sb.toString()
    }

    // Kept for backward compatibility if needed
    fun generateFixtures(participants: List<String>): List<Match> {
        return emptyList()
    }
}

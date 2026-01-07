package com.sbuddy.app.utils

import com.sbuddy.app.data.model.Match

class TournamentManager {

    /**
     * Generates a single-elimination fixture list.
     * Takes a list of participant names and returns a list of Matches for the first round.
     */
    fun generateFixtures(participants: List<String>): List<Match> {
        if (participants.size < 2) return emptyList()

        val shuffled = participants.shuffled() // Random pairing
        val matches = mutableListOf<Match>()

        for (i in 0 until shuffled.size step 2) {
            if (i + 1 < shuffled.size) {
                matches.add(
                    Match(
                        id = "match_$i",
                        player1Name = shuffled[i],
                        player2Name = shuffled[i+1],
                        player1Score = 0,
                        player2Score = 0
                    )
                )
            } else {
                // Bye for the last player if odd number
                matches.add(
                    Match(
                        id = "bye_$i",
                        player1Name = shuffled[i],
                        player2Name = "BYE",
                        winner = shuffled[i]
                    )
                )
            }
        }
        return matches
    }
}

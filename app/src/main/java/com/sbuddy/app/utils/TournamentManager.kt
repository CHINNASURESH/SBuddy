package com.sbuddy.app.utils

import com.sbuddy.app.data.model.Match
import java.util.UUID

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

    /**
     * Generates a League format with Group Stages.
     * Splits participants into groups (approx 4-5 per group) and generates Round Robin for each.
     */
    fun generateLeagueText(participants: List<String>): String {
        if (participants.size < 3) return generateRoundRobinText(participants)

        val sb = StringBuilder()

        // Determine number of groups. Target size 4-5.
        // If 6 players -> 2 groups of 3.
        val groupCount = if (participants.size > 5) (participants.size + 3) / 4 else 1

        if (groupCount == 1) {
            sb.append("=== LEAGUE STAGE ===\n\n")
            sb.append(generateRoundRobinText(participants))
            sb.append("\n=== FINAL ===\n")
            sb.append("Match: 1st Place vs 2nd Place\n")
            sb.append("Winner: ____________\n")
            sb.append("Runner-up: ____________\n")
            return sb.toString()
        }

        // Shuffle and split
        val shuffled = participants.shuffled()
        val groups = MutableList(groupCount) { mutableListOf<String>() }

        shuffled.forEachIndexed { index, p ->
            groups[index % groupCount].add(p)
        }

        groups.forEachIndexed { index, groupMembers ->
            val groupName = (65 + index).toChar() // A, B, C...
            sb.append("=== GROUP $groupName ===\n")
            sb.append(generateRoundRobinText(groupMembers))
            sb.append("\n")
        }

        sb.append("=== KNOCKOUT STAGE ===\n\n")
        if (groupCount == 2) {
            sb.append("Semi-Final 1: Winner Grp A vs Runner-up Grp B\n")
            sb.append("Semi-Final 2: Winner Grp B vs Runner-up Grp A\n\n")
            sb.append("Final: Winner SF1 vs Winner SF2\n")
            sb.append("Winner: ____________\n")
            sb.append("Runner-up: ____________\n")
        } else if (groupCount >= 4) {
            sb.append("Quarter-Finals: Winners vs Runners-up (Cross Group)\n")
            sb.append("Semi-Finals: Winner QF1 vs Winner QF2...\n")
            sb.append("Final: Winner SF1 vs Winner SF2\n")
        } else {
            // 3 groups or generic
            sb.append("Semi-Finals & Finals (Check rules for 3 groups)\n")
        }

        return sb.toString()
    }

    /**
     * Generates structured List<Match> for the tournament.
     */
    fun generateFixturesList(participants: List<String>, type: String, topSeed: String? = null): List<Match> {
        return if (type == "League") {
            generateLeagueMatches(participants)
        } else {
            generateKnockoutMatches(participants, topSeed)
        }
    }

    private data class Slot(val name: String, val sourceMatchId: String? = null)

    private fun generateKnockoutMatches(participants: List<String>, topSeed: String?): List<Match> {
        val matches = mutableListOf<Match>()
        if (participants.size < 2) return matches

        val workingList = participants.toMutableList()
        // Handle seeding: move topSeed to index 0
        if (topSeed != null && workingList.contains(topSeed)) {
            workingList.remove(topSeed)
            workingList.shuffle()
            workingList.add(0, topSeed)
        } else {
            workingList.shuffle()
        }

        var currentRoundSlots = workingList.map { Slot(it) }
        var matchGlobalCount = 1

        while (currentRoundSlots.size > 1) {
            val nextRoundSlots = mutableListOf<Slot>()
            val matchCount = (currentRoundSlots.size + 1) / 2

            for (i in 0 until matchCount) {
                val p1Index = i * 2
                val p2Index = i * 2 + 1

                if (p2Index < currentRoundSlots.size) {
                    val slot1 = currentRoundSlots[p1Index]
                    val slot2 = currentRoundSlots[p2Index]

                    val matchId = UUID.randomUUID().toString()
                    val matchLabel = "M$matchGlobalCount"

                    matches.add(Match(
                        id = matchId,
                        matchLabel = matchLabel,
                        player1Name = slot1.name,
                        player2Name = slot2.name,
                        sourceMatchId1 = slot1.sourceMatchId,
                        sourceMatchId2 = slot2.sourceMatchId,
                        winner = null
                    ))

                    nextRoundSlots.add(Slot("Winner $matchLabel", matchId))
                    matchGlobalCount++
                } else {
                    // Bye - passes through
                    nextRoundSlots.add(currentRoundSlots[p1Index])
                }
            }
            currentRoundSlots = nextRoundSlots
        }
        return matches
    }

    private fun generateLeagueMatches(participants: List<String>): List<Match> {
        val matches = mutableListOf<Match>()
        val groupCount = if (participants.size > 5) (participants.size + 3) / 4 else 1

        if (groupCount == 1) {
            return generateRoundRobinMatches(participants, "League")
        }

        val shuffled = participants.shuffled()
        val groups = MutableList(groupCount) { mutableListOf<String>() }
        shuffled.forEachIndexed { index, p -> groups[index % groupCount].add(p) }

        groups.forEachIndexed { index, groupMembers ->
            val groupName = (65 + index).toChar().toString()
            matches.addAll(generateRoundRobinMatches(groupMembers, "Group $groupName"))
        }

        return matches
    }

    private fun generateRoundRobinMatches(participants: List<String>, contextName: String): List<Match> {
        val matches = mutableListOf<Match>()
        if (participants.size < 2) return matches

        val players = participants.toMutableList()
        if (players.size % 2 != 0) {
            players.add("BYE")
        }

        val numRounds = players.size - 1
        val halfSize = players.size / 2
        var matchGlobalCount = 1
        val teamSize = players.size

        for (round in 0 until numRounds) {
            for (i in 0 until halfSize) {
                val p1 = players[i]
                val p2 = players[teamSize - 1 - i]

                if (p1 != "BYE" && p2 != "BYE") {
                    val matchId = UUID.randomUUID().toString()
                    val label = "$contextName M$matchGlobalCount"

                    matches.add(Match(
                        id = matchId,
                        matchLabel = label,
                        player1Name = p1,
                        player2Name = p2
                    ))
                    matchGlobalCount++
                }
            }
            // Rotate
            val last = players.removeAt(players.size - 1)
            players.add(1, last)
        }
        return matches
    }

    // Deprecated
    fun generateFixtures(participants: List<String>): List<Match> {
        return generateFixturesList(participants, "Knockout")
    }
}

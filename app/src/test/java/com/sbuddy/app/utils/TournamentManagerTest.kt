package com.sbuddy.app.utils

import org.junit.Test
import org.junit.Assert.*

class TournamentManagerTest {

    private val manager = TournamentManager()

    @Test
    fun testGenerateBracketText() {
        val participants = listOf("A", "B", "C", "D")
        val bracketText = manager.generateBracketText(participants)

        assertTrue(bracketText.contains("=== ROUND 1 ==="))
        assertTrue(bracketText.contains("Match 1:"))
        assertTrue(bracketText.contains("Match 2:"))
        assertTrue(bracketText.contains("=== WINNER ==="))
    }

    @Test
    fun testGenerateBracketTextOdd() {
        val participants = listOf("A", "B", "C")
        val bracketText = manager.generateBracketText(participants)

        // My implementation uses "Bye: Player (Advances)"
        // Case-insensitive check or check for specific string
        assertTrue(bracketText.contains("Bye", ignoreCase = true))
    }

    @Test
    fun testGenerateLeagueTextSmall() {
        // Less than or equal 5 -> Single group
        val participants = listOf("A", "B", "C", "D")
        val text = manager.generateLeagueText(participants)
        assertTrue(text.contains("=== LEAGUE STAGE ==="))
        assertFalse(text.contains("=== GROUP A ==="))
    }

    @Test
    fun testGenerateLeagueTextLarge() {
        // More than 5 -> Multiple groups
        val participants = listOf("A", "B", "C", "D", "E", "F")
        val text = manager.generateLeagueText(participants)
        // 6 players -> 2 groups of 3
        assertTrue(text.contains("=== GROUP A ==="))
        assertTrue(text.contains("=== GROUP B ==="))
        // Group B should exist
    }
}

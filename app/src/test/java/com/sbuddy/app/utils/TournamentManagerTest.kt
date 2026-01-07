package com.sbuddy.app.utils

import org.junit.Test
import org.junit.Assert.*

class TournamentManagerTest {

    private val manager = TournamentManager()

    @Test
    fun testGenerateFixturesEven() {
        val participants = listOf("A", "B", "C", "D")
        val fixtures = manager.generateFixtures(participants)

        assertEquals(2, fixtures.size)
        // Check that all players are assigned
        val assigned = fixtures.flatMap { listOf(it.player1Name, it.player2Name) }
        assertTrue(assigned.containsAll(participants))
    }

    @Test
    fun testGenerateFixturesOdd() {
        val participants = listOf("A", "B", "C")
        val fixtures = manager.generateFixtures(participants)

        // Should have 1 match (A vs B) and 1 Bye match or similar logic depending on implementation.
        // My implementation adds a "BYE" match for the odd one out.
        assertEquals(2, fixtures.size)

        val lastMatch = fixtures.last()
        assertEquals("BYE", lastMatch.player2Name)
        assertNotNull(lastMatch.winner)
    }
}

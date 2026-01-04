package com.sbuddy.app.utils

import org.junit.Test
import org.junit.Assert.*

class GameLogicTest {

    private val gameLogic = GameLogic()

    @Test
    fun testServerSide() {
        // Score 0-0 (Even) -> Right
        assertEquals(GameLogic.ServerSide.RIGHT, gameLogic.getServerSide(0))

        // Score 1-0 (Odd) -> Left
        assertEquals(GameLogic.ServerSide.LEFT, gameLogic.getServerSide(1))

        // Score 2-1 (Odd) -> Left
        assertEquals(GameLogic.ServerSide.LEFT, gameLogic.getServerSide(3))

        // Score 2-2 (Even) -> Right
        assertEquals(GameLogic.ServerSide.RIGHT, gameLogic.getServerSide(4))
    }

    @Test
    fun testServiceStatus() {
        gameLogic.setRules(21, "Player 1", "Player 2", false)

        // P1 serving, 0-0 (Even) -> Right
        assertEquals("Player 1 serving from Right Court", gameLogic.getServiceStatus())

        // P1 wins point -> 1-0. P1 serving from Left
        gameLogic.addPoint("Player 1")
        assertEquals("Player 1 serving from Left Court", gameLogic.getServiceStatus())
    }

    @Test
    fun test15PointGame() {
        gameLogic.setRules(15, "Alice", "Bob", false)

        // Advance Alice to 14
        for (i in 1..14) gameLogic.addPoint("Alice")
        assertEquals(14, gameLogic.getScoreP1())
        assertFalse(gameLogic.isGameOver())

        // Alice wins
        gameLogic.addPoint("Alice")
        assertTrue(gameLogic.isGameOver())
        assertEquals("Alice", gameLogic.getWinner())
    }
}

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
        // P1 serving, even score
        assertEquals("Player 1 serving from Right Court", gameLogic.getServiceStatus("Player 1", 0))

        // P1 serving, odd score
        assertEquals("Player 1 serving from Left Court", gameLogic.getServiceStatus("Player 1", 1))
    }
}

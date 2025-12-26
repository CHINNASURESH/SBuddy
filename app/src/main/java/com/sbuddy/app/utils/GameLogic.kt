package com.sbuddy.app.utils

import java.util.Stack

class GameLogic {

    enum class ServerSide {
        RIGHT, LEFT
    }

    data class GameState(
        val scoreP1: Int,
        val scoreP2: Int,
        val server: String,
        val isGameOver: Boolean,
        val winner: String?
    )

    private var scoreP1 = 0
    private var scoreP2 = 0
    private var currentServer = "Player 1" // Ideally this would be an enum or ID
    private var history = Stack<GameState>()

    // Constants
    private val POINTS_TO_WIN = 21
    private val MAX_POINTS = 30

    fun getScoreP1() = scoreP1
    fun getScoreP2() = scoreP2
    fun getCurrentServer() = currentServer

    fun resetGame(startingServer: String = "Player 1") {
        scoreP1 = 0
        scoreP2 = 0
        currentServer = startingServer
        history.clear()
    }

    fun addPoint(winner: String) {
        // Save current state to history before modifying
        saveState()

        if (isGameOver()) return

        if (winner == "Player 1") {
            scoreP1++
            currentServer = "Player 1"
        } else {
            scoreP2++
            currentServer = "Player 2"
        }
    }

    fun undo() {
        if (history.isNotEmpty()) {
            val lastState = history.pop()
            scoreP1 = lastState.scoreP1
            scoreP2 = lastState.scoreP2
            currentServer = lastState.server
        }
    }

    private fun saveState() {
        history.push(GameState(scoreP1, scoreP2, currentServer, isGameOver(), getWinner()))
    }

    fun isGameOver(): Boolean {
        if (scoreP1 >= MAX_POINTS || scoreP2 >= MAX_POINTS) return true
        if (scoreP1 >= POINTS_TO_WIN && (scoreP1 - scoreP2) >= 2) return true
        if (scoreP2 >= POINTS_TO_WIN && (scoreP2 - scoreP1) >= 2) return true
        return false
    }

    fun getWinner(): String? {
        if (!isGameOver()) return null
        return if (scoreP1 > scoreP2) "Player 1" else "Player 2"
    }

    /**
     * Determines the current server side based on the score.
     * In badminton, if the score is even, serve from the right. If odd, serve from the left.
     */
    fun getServerSide(score: Int): ServerSide {
        return if (score % 2 == 0) ServerSide.RIGHT else ServerSide.LEFT
    }

    /**
     * AI Server Highlighting logic.
     * Returns a string description of who should be serving and from which side.
     */
    fun getServiceStatus(): String {
        if (isGameOver()) {
            return "Game Over! Winner: ${getWinner()}"
        }
        val serverScore = if (currentServer == "Player 1") scoreP1 else scoreP2
        val side = getServerSide(serverScore)
        val sideString = if (side == ServerSide.RIGHT) "Right Court" else "Left Court"
        return "$currentServer serving from $sideString"
    }
}

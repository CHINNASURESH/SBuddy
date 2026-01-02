package com.sbuddy.app.utils

import com.sbuddy.app.data.model.Match
import com.sbuddy.app.data.repository.MatchRepository
import java.util.Stack
import java.util.UUID

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

    // Configurable Rules
    private var pointsToWin = 21
    private var maxPoints = 30
    private var p1Name = "Player 1"
    private var p2Name = "Player 2"
    private var isDoubles = false

    fun setRules(targetScore: Int, player1: String, player2: String, doubles: Boolean) {
        pointsToWin = targetScore
        // Simple rule: Cap is +9 of target, or fixed 30? Standard is 30.
        // If target is 30, cap should probably be 30.
        maxPoints = if (targetScore >= 30) 30 else 30
        p1Name = player1.ifBlank { "Player 1" }
        p2Name = player2.ifBlank { "Player 2" }
        isDoubles = doubles

        // Reset game with new settings
        resetGame(p1Name)
    }

    fun getScoreP1() = scoreP1
    fun getScoreP2() = scoreP2
    fun getP1Name() = p1Name
    fun getP2Name() = p2Name
    fun getCurrentServer() = currentServer

    fun resetGame(startingServer: String? = null) {
        scoreP1 = 0
        scoreP2 = 0
        currentServer = startingServer ?: p1Name
        history.clear()
    }

    fun addPoint(winner: String) {
        // Save current state to history before modifying
        saveState()

        if (isGameOver()) return

        if (winner == p1Name) {
            scoreP1++
            currentServer = p1Name
        } else {
            scoreP2++
            currentServer = p2Name
        }

        if (isGameOver()) {
            saveMatchToHistory()
        }
    }

    private fun saveMatchToHistory() {
        val match = Match(
            id = UUID.randomUUID().toString(),
            player1Name = p1Name,
            player2Name = p2Name,
            player1Score = scoreP1,
            player2Score = scoreP2,
            winner = getWinner(),
            isDoubles = isDoubles
        )
        MatchRepository.addMatch(match)
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
        if (scoreP1 >= maxPoints || scoreP2 >= maxPoints) return true
        if (scoreP1 >= pointsToWin && (scoreP1 - scoreP2) >= 2) return true
        if (scoreP2 >= pointsToWin && (scoreP2 - scoreP1) >= 2) return true
        return false
    }

    fun getWinner(): String? {
        if (!isGameOver()) return null
        return if (scoreP1 > scoreP2) p1Name else p2Name
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
        val serverScore = if (currentServer == p1Name) scoreP1 else scoreP2
        val side = getServerSide(serverScore)
        val sideString = if (side == ServerSide.RIGHT) "Right Court" else "Left Court"
        return "$currentServer serving from $sideString"
    }
}

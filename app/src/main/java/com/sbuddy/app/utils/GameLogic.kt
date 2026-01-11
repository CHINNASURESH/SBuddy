package com.sbuddy.app.utils

class GameLogic {

    enum class ServerSide {
        RIGHT, LEFT
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
    fun getServiceStatus(serverName: String, score: Int): String {
        val side = getServerSide(score)
        val sideString = if (side == ServerSide.RIGHT) "Right Court" else "Left Court"
        return "$serverName serving from $sideString"
    }
}

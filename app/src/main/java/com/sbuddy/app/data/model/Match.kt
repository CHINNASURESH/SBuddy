package com.sbuddy.app.data.model

import androidx.annotation.Keep

@Keep
data class Match(
    val id: String = "",
    val player1Name: String = "",
    val player2Name: String = "",
    val player1Score: Int = 0,
    val player2Score: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val winner: String? = null,
    val isSingles: Boolean = false,
    val matchLabel: String = "",
    val sourceMatchId1: String? = null,
    val sourceMatchId2: String? = null
)

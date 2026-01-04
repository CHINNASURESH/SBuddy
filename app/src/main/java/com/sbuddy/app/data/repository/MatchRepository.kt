package com.sbuddy.app.data.repository

import com.sbuddy.app.data.model.Match

object MatchRepository {
    private val matches = mutableListOf<Match>()

    fun addMatch(match: Match) {
        matches.add(match)
    }

    fun getMatches(): List<Match> {
        return matches.toList().reversed()
    }
}

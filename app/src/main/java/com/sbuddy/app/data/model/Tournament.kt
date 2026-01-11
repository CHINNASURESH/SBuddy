package com.sbuddy.app.data.model

data class Tournament(
    val id: String = "",
    val name: String = "",
    val participants: List<String> = emptyList(),
    val rounds: List<Match> = emptyList()
)

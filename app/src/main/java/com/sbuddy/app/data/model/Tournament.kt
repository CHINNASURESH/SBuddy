package com.sbuddy.app.data.model

data class Tournament(
    val id: String = "",
    val name: String = "",
    val creatorId: String = "",
    val date: Long = 0,
    val participants: List<String> = emptyList(),
    val rounds: List<Match> = emptyList(),
    val bracketText: String = "",
    @field:JvmField
    val isPublic: Boolean = true,
    val imageUrl: String = "",
    val location: String = "",
    val status: String = "Open"
)

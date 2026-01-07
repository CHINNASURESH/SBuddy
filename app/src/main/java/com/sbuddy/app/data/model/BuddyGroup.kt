package com.sbuddy.app.data.model

data class BuddyGroup(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val members: MutableList<String> = mutableListOf()
) {
    val memberCount: Int
        get() = members.size
}

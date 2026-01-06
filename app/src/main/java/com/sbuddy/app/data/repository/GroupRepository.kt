package com.sbuddy.app.data.repository

import com.sbuddy.app.data.model.BuddyGroup

class GroupRepository {

    // Mock data
    private val groups = mutableListOf(
        BuddyGroup("1", "Weekend Warriors", "Casual badminton every Saturday", 12),
        BuddyGroup("2", "Pro Smashers", "Competitive league players", 8),
        BuddyGroup("3", "Office Team", "Colleagues badminton club", 24)
    )

    fun getGroups(): List<BuddyGroup> {
        return groups
    }

    fun createGroup(name: String, description: String) {
        val newGroup = BuddyGroup(
            id = (groups.size + 1).toString(),
            name = name,
            description = description,
            memberCount = 1 // Creator
        )
        groups.add(newGroup)
    }
}

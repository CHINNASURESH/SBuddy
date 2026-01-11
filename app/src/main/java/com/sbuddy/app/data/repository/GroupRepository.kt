package com.sbuddy.app.data.repository

import com.sbuddy.app.data.model.BuddyGroup

class GroupRepository {

    // Mock data
    companion object {
        private val groups = mutableListOf(
            BuddyGroup("1", "Weekend Warriors", "Casual badminton every Saturday", mutableListOf("Alice", "Bob", "Charlie", "Dave")),
            BuddyGroup("2", "Pro Smashers", "Competitive league players", mutableListOf("Eve", "Frank", "Grace", "Heidi")),
            BuddyGroup("3", "Office Team", "Colleagues badminton club", mutableListOf("Ivan", "Judy", "Mallory", "Niaj"))
        )
    }

    fun getGroups(): List<BuddyGroup> {
        return groups
    }

    fun getGroupById(id: String): BuddyGroup? {
        return groups.find { it.id == id }
    }

    fun createGroup(name: String, description: String) {
        val newGroup = BuddyGroup(
            id = (groups.size + 1).toString(),
            name = name,
            description = description,
            members = mutableListOf("Current User") // Creator
        )
        groups.add(newGroup)
    }

    fun addMember(groupId: String, memberName: String) {
        val group = groups.find { it.id == groupId }
        group?.members?.add(memberName)
    }
}

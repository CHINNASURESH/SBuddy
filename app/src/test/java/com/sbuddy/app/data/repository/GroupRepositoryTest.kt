package com.sbuddy.app.data.repository

import org.junit.Test
import org.junit.Assert.*

class GroupRepositoryTest {

    private val repository = GroupRepository()

    @Test
    fun testGetGroups() {
        val groups = repository.getGroups()
        assertTrue(groups.isNotEmpty())
        assertTrue(groups.any { it.name == "Weekend Warriors" })
    }

    @Test
    fun testGetGroupById_Existing() {
        val group = repository.getGroupById("1")
        assertNotNull(group)
        assertEquals("Weekend Warriors", group?.name)
    }

    @Test
    fun testGetGroupById_NonExisting() {
        val group = repository.getGroupById("9999")
        assertNull(group)
    }

    @Test
    fun testCreateGroup() {
        val initialSize = repository.getGroups().size
        repository.createGroup("New Test Group", "Test Description")

        val newSize = repository.getGroups().size
        assertEquals(initialSize + 1, newSize)

        val createdGroup = repository.getGroups().last()
        assertEquals("New Test Group", createdGroup.name)
        assertEquals("Test Description", createdGroup.description)
        assertTrue(createdGroup.members.contains("Current User"))
    }

    @Test
    fun testAddMember() {
        val groupId = "1"
        val newMember = "New Member"

        repository.addMember(groupId, newMember)

        val group = repository.getGroupById(groupId)
        assertNotNull(group)
        assertTrue(group!!.members.contains(newMember))
    }
}

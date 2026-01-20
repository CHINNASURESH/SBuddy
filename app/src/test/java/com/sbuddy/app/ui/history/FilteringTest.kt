package com.sbuddy.app.ui.history

import com.sbuddy.app.data.model.Match
import org.junit.Assert.assertEquals
import org.junit.Test

class FilteringTest {
    @Test
    fun testFilteringLogic() {
        val allMatches = listOf(
            Match(id = "1", isSingles = true),
            Match(id = "2", isSingles = false),
            Match(id = "3", isSingles = true),
            Match(id = "4", isSingles = false),
            Match(id = "5", isSingles = false)
        )

        // 0 = All, 1 = Singles, 2 = Doubles
        fun filter(tab: Int): List<Match> {
            return when (tab) {
                0 -> allMatches
                1 -> allMatches.filter { it.isSingles }
                2 -> allMatches.filter { !it.isSingles }
                else -> allMatches
            }
        }

        // Test All
        val all = filter(0)
        assertEquals(5, all.size)

        // Test Singles
        val singles = filter(1)
        assertEquals(2, singles.size)
        assertEquals("1", singles[0].id)
        assertEquals("3", singles[1].id)

        // Test Doubles
        val doubles = filter(2)
        assertEquals(3, doubles.size)
        assertEquals("2", doubles[0].id)
        assertEquals("4", doubles[1].id)
        assertEquals("5", doubles[2].id)
    }
}

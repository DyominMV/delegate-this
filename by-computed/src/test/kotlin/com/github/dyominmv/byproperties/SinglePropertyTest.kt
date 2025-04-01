package com.github.dyominmv.byproperties

import com.github.dyominmv.byproperties.ComparedProperty.Companion.comparedProperty
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SinglePropertyTest {
    @Test
    fun `comparison by single property should work correctly`() {
        class ComparedSample(
            var id: Long? = null,
            var name: String,
        ): Comparable<ComparedSample> by comparedProperty(ComparedSample::name)

        val s1 = ComparedSample(null, "1")
        val s2 = ComparedSample(null, "2")
        assertFalse { s1 >= s2 }
        assertTrue { s1 < s2 }

        s1.name = "3"
        assertFalse { s1 <= s2 }
        assertTrue { s1 > s2 }

        s1.name = s2.name

        assertFalse { s1 < s2 }
        assertFalse { s1 > s2 }
        assertTrue { s1.compareTo(s2) == 0 }
    }
}
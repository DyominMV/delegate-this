package com.github.dyominmv.byproperties

import com.github.dyominmv.byproperties.NullableProperty.Companion.nullableProperty
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NullablePropertyTest {
    @Test
    fun `equals and toString by nullable property should work correctly`() {
        class Sample(
            var id: Long? = null,
            val name: String,
        ): EqualsHashcodeAndToString by nullableProperty(Sample::id)

        val s1 = Sample(null, "first")
        val s2 = Sample(null, "second")
        assertFalse { s1 == s2 }

        s1.id = 115_995
        assertFalse { s1 == s2 }

        s2.id = s1.id
        assertTrue { s1 == s2 }

        assertContains(s1.toString(), s1.id.toString())
    }
}
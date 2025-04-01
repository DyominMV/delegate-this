package com.github.dyominmv.byproperties

import com.github.dyominmv.byproperties.Properties.Companion.properties
import org.junit.jupiter.api.Test
import kotlin.test.assertContains

class SimpleSample(
    var name: String,
    var age: Int,
) : EqualsHashcodeAndToString by properties(SimpleSample::name, SimpleSample::age)

class PropertiesTest {
    @Test
    fun `delegating to two properties should work correctly`() {
        val simpleSample = SimpleSample("Name", 115)
        assertContains(simpleSample.toString(), "115")
        assertContains(simpleSample.toString(), "Name")

        simpleSample.age = 250
        assertContains(simpleSample.toString(), "250")
    }
}
package mikhail.dyomin.mavenexample

import mikhail.dyomin.mavenexample.ComparedProperty.Companion.comparedProperty
import mikhail.dyomin.mavenexample.Property.Companion.property
import org.junit.jupiter.api.Test
import kotlin.reflect.KProperty0
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SampleWithVariable(
    var memes: String
) : EqualsHashcodeAndComparable<SampleWithVariable> by comparedProperty(SampleWithVariable::memes),
    ToString by property(SampleWithVariable::id) {
    val id: Int = nextId.also { nextId += 1 }

    companion object {
        private var nextId: Int = 1
    }
}

class DelegatePluginTest {
    @Test
    fun `hashCode by property should work correctly`() {
        val sample1 = SampleWithVariable("sample1")
        val sample2 = SampleWithVariable("sample2")

        assertNotEquals(sample2, sample1)

        sample1.memes = "sample"
        sample2.memes = "s" + "a" + "m" + "p" + "l" + "e"

        assertEquals(sample1, sample2)
        assertEquals(sample1.hashCode(), sample2.hashCode())
        assertEquals(sample1.hashCode(), "sample".hashCode())
    }
}

fun main() {
    println(SampleWithVariable::memes)
    val s = SampleWithVariable("ss")
    val x: KProperty0<*> = s::memes
    println(x.name)
}
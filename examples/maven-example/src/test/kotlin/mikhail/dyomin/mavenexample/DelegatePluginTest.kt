package mikhail.dyomin.mavenexample

import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

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

        assertContains(sample1.toString(), "1")
        assertContains(sample2.toString(), "2")
    }

    @Test
    fun `Comparable{String} should be delegated to computed shrimpsId`() {
        val shrimper = Shrimper(representation = "🦐", count = 7)
        assertTrue { shrimper > 5 }
        shrimper.count = 4
        assertTrue { shrimper < 5 }

    }
}
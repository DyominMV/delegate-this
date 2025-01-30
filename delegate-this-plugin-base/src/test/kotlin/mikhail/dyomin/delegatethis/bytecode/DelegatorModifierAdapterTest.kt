package mikhail.dyomin.delegatethis.bytecode

import mikhail.dyomin.delegatethis.samples.NameExtractorDelegate
import mikhail.dyomin.delegatethis.samples.SampleInterface
import mikhail.dyomin.delegatethis.samples.loadClass
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DelegatorModifierAdapterTest {
    @ParameterizedTest
    @MethodSource("classes with delegate")
    fun `DelegatorModifierAdapter should add method called 'delegateThis'`(modifiedClass: Class<*>): Unit =
        assertDoesNotThrow { modifiedClass.getDeclaredMethod("\$delegate_this!") }

    @ParameterizedTest
    @MethodSource("classes with delegate")
    fun `DelegatorModifierAdapter should create constructors with Void param`(modifiedClass: Class<*>): Unit =
        modifiedClass.declaredConstructors.filter {
            it.parameterTypes.lastOrNull() != Nothing::class.java
        }.forEach {
            val ctor = assertDoesNotThrow {
                modifiedClass.getDeclaredConstructor(*it.parameterTypes, Nothing::class.java)
            }

            assertTrue { ctor.modifiers and Modifier.PRIVATE == Modifier.PRIVATE }
        }

    @Test
    fun `ClassWithSimpleDelegate should be comparable`(): Unit = assertDoesNotThrow {
        val instance = Class.forName("mikhail.dyomin.delegatethis.samples.ClassWithSimpleDelegate")
            .getDeclaredConstructor(String::class.java)
            .newInstance("dogs")

        @Suppress("UNCHECKED_CAST")
        (instance as Comparable<String>).compareTo("other")
    }

    @Test
    fun `ClassWithDeeperDelegate should have name`(): Unit = assertDoesNotThrow {
        val instance = Class.forName("mikhail.dyomin.delegatethis.samples.ClassWithDeeperDelegate")
            .getDeclaredConstructor(NameExtractorDelegate::class.java, Int::class.java)
            .newInstance(NameExtractorDelegate(), 100)

        assertNotNull((instance as SampleInterface).name())
    }

    @Test
    fun `sadly, but ClassWithTiedDelegates should throw exception as lateinit var is not initialized yet`() {
        assertThrows<InvocationTargetException> {
            Class.forName("mikhail.dyomin.delegatethis.samples.ClassWithTiedDelegates")
                .getDeclaredConstructor(List::class.java)
                .newInstance(listOf("x", "y", "shrimp"))
        }
    }

    companion object {
        private val sampleClassNames = listOf(
            "mikhail.dyomin.delegatethis.samples.ClassWithDeeperDelegate",
            "mikhail.dyomin.delegatethis.samples.ClassWithSimpleDelegate",
            "mikhail.dyomin.delegatethis.samples.ClassWithTiedDelegates",
        )

        @JvmStatic
        fun `classes with delegate`() = sampleClassNames.map { Class.forName(it) }

        @BeforeAll
        @JvmStatic
        fun `modify and load all sample classes`() {
            val factory = ClassFileFactory { name -> ClassReader(name) }

            sampleClassNames.forEach { className ->
                val reader = factory.getClassFile(className).createReader()
                val writer = ClassWriter(reader, ClassWriter.COMPUTE_MAXS)
                reader.accept(
                    DelegatorModifierAdapter(factory.getClassFile(className), writer),
                    0
                )

                loadClass(writer.toByteArray())
            }
        }
    }
}
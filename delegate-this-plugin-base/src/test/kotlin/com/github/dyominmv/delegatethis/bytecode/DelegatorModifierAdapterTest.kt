package com.github.dyominmv.delegatethis.bytecode

import com.github.dyominmv.delegatethis.AlreadyModified
import com.github.dyominmv.delegatethis.NonDelegatingConstructorMarker
import com.github.dyominmv.delegatethis.DelegateThis
import com.github.dyominmv.delegatethis.samples.NameExtractorDelegate
import com.github.dyominmv.delegatethis.samples.SampleInterface
import com.github.dyominmv.delegatethis.samples.loadClass
import org.junit.jupiter.api.*
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.objectweb.asm.ClassReader
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@TestInstance(PER_CLASS)
class DelegatorModifierAdapterTest {

    @BeforeAll
    fun `modify and load all sample classes`() {
        val resourcesRoot = File(javaClass.getResource("")!!.file).toPath()
        val delegateThis = DelegateThis(listOf(resourcesRoot))

        sampleClassNames.forEach { className ->
            val reader = ClassReader(className)
            loadClass(delegateThis.run {
                reader.addDelegatesInitialization(getMetadata(className).fields.getDelegatesOnly())
            })
        }
    }

    @ParameterizedTest
    @MethodSource("classes with delegate")
    fun `DelegatorModifierAdapter should add method called 'delegateThis'`(modifiedClass: Class<*>): Unit =
        assertDoesNotThrow { modifiedClass.getDeclaredMethod("\$delegate_this!") }

    @ParameterizedTest
    @MethodSource("classes with delegate")
    fun `DelegatorModifierAdapter should create constructors with Void param`(modifiedClass: Class<*>): Unit =
        modifiedClass.declaredConstructors.filter {
            it.parameterTypes.lastOrNull() != NonDelegatingConstructorMarker::class.java
        }.forEach {
            val ctor = assertDoesNotThrow {
                modifiedClass.getDeclaredConstructor(*it.parameterTypes, NonDelegatingConstructorMarker::class.java)
            }

            assertTrue { ctor.modifiers and Modifier.PRIVATE == Modifier.PRIVATE }
        }

    @Test
    fun `ClassWithSimpleDelegate should be comparable`(): Unit = assertDoesNotThrow {
        val instance = Class.forName("com.github.dyominmv.delegatethis.samples.ClassWithSimpleDelegate")
            .getDeclaredConstructor(String::class.java)
            .newInstance("dogs")

        @Suppress("UNCHECKED_CAST")
        assertDoesNotThrow {
            (instance as Comparable<String>).compareTo("other")
        }
    }

    @Test
    fun `ClassWithDeeperDelegate should have name`(): Unit = assertDoesNotThrow {
        val instance = Class.forName("com.github.dyominmv.delegatethis.samples.ClassWithDeeperDelegate")
            .getDeclaredConstructor(NameExtractorDelegate::class.java, Int::class.java)
            .newInstance(NameExtractorDelegate(), 100)

        assertNotNull((instance as SampleInterface).name())
    }

    @Test
    fun `sadly, but ClassWithTiedDelegates should throw exception as lateinit var is not initialized yet`() {
        assertThrows<InvocationTargetException> {
            Class.forName("com.github.dyominmv.delegatethis.samples.ClassWithTiedDelegates")
                .getDeclaredConstructor(List::class.java)
                .newInstance(listOf("x", "y", "shrimp"))
        }
    }


    @ParameterizedTest
    @MethodSource("classes with delegate")
    fun `DelegatorModifierAdapter should add annotation to a class`(modifiedClass: Class<*>) {
        assertTrue { modifiedClass.isAnnotationPresent(AlreadyModified::class.java) }
    }

    private val sampleClassNames = listOf(
        "com.github.dyominmv.delegatethis.samples.ClassWithDeeperDelegate",
        "com.github.dyominmv.delegatethis.samples.ClassWithSimpleDelegate",
        "com.github.dyominmv.delegatethis.samples.ClassWithTiedDelegates",
    )

    fun `classes with delegate`() = sampleClassNames.map { Class.forName(it) }

}
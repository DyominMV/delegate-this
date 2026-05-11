package io.github.dyominmv.delegatethis.bytecode

import io.github.dyominmv.delegatethis.*
import io.github.dyominmv.delegatethis.samples.NamedObject
import io.github.dyominmv.delegatethis.samples.NoMethods
import io.github.dyominmv.delegatethis.samples.NoMethodsDelegate
import io.github.dyominmv.delegatethis.samples.loadClass
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.lang.ClassLoader.getSystemClassLoader
import java.lang.constant.ClassDesc
import java.lang.reflect.Modifier

@TestInstance(PER_CLASS)
class ClassModifierTest {

    private val User = "io.github.dyominmv.delegatethis.samples.User"
    private val Delegator = "io.github.dyominmv.delegatethis.samples.Delegator"
    private val ImplicitDelegator = "io.github.dyominmv.delegatethis.samples.ImplicitDelegator"
    private val DelegateThisUnawareDelegator = "io.github.dyominmv.delegatethis.samples.DelegateThisUnawareDelegator"
    private val sampleClassNames = listOf(User, Delegator, ImplicitDelegator, DelegateThisUnawareDelegator)

    private object BytecodeModifier {
        val classModelProvider = ClassModelProvider(emptyList(), getSystemClassLoader())
        val delegateCache = DelegateCache(classModelProvider)
        val classModifier = ClassModifier(delegateCache)

        fun modifyAndLoadIfNeeded(className: String) {
            val model = ClassDesc.of(className).let { classModelProvider.loadClassModel(it) }
            classModifier.modifyClassIfNeeded(model) { loadClass(it) }
        }
    }

    private fun `classes with delegate`() = listOf(User, Delegator, ImplicitDelegator)
        .map { Class.forName(it) }

    @BeforeAll
    fun `modify and load all sample classes`() = sampleClassNames.forEach(BytecodeModifier::modifyAndLoadIfNeeded)

    @ParameterizedTest
    @MethodSource("classes with delegate")
    fun `modified classes should have private method called 'delegateThis'`(modifiedClass: Class<*>): Unit =
        shouldNotThrowAny { modifiedClass.getDeclaredMethod($$"$delegate_this!") }

    @ParameterizedTest
    @MethodSource("classes with delegate")
    fun `modified class constructors should be in pairs (shadowed and original)`(modifiedClass: Class<*>) {
        modifiedClass.declaredConstructors
            .filter { it.parameterTypes.lastOrNull() != NonDelegatingConstructorMarker::class.java }
            .forEach { originalConstructor ->
                val modifiedConstructor = shouldNotThrowAny {
                    modifiedClass.getDeclaredConstructor(
                        *originalConstructor.parameterTypes,
                        NonDelegatingConstructorMarker::class.java
                    )
                }
                (modifiedConstructor.modifiers and Modifier.PRIVATE) shouldBe Modifier.PRIVATE
            }
    }

    @ParameterizedTest
    @MethodSource("classes with delegate")
    fun `modified class should be annotated as AlreadyModified`(modifiedClass: Class<*>) {
        modifiedClass.isAnnotationPresent(AlreadyModified::class.java).shouldBeTrue()
    }

    @Test
    fun `NoMethodsDelegate should receive delegate reference`() {
        val delegatorClass = Class.forName(Delegator)
        val delegate = NoMethodsDelegate()
        val delegator = delegatorClass.getConstructor(NoMethodsDelegate::class.java).newInstance(delegate)

        delegate.delegator shouldBe delegator
    }

    @Test
    fun `ImplicitDelegator should pass its reference to delegate`() {
        val implicitDelegatorClass = Class.forName(ImplicitDelegator)
        val delegator = implicitDelegatorClass.getConstructor().newInstance()

        implicitDelegatorClass.declaredFields shouldHaveSize 1
        val delegate = implicitDelegatorClass.declaredFields[0].run {
            trySetAccessible()
            get(delegator) as NoMethodsDelegate
        }

        delegate.delegator shouldBe delegator
    }

    @Test
    fun `DelegateThisUnawareDelegator should not pass its reference to delegate`() {
        val delegateThisUnawareDelegatorClass = Class.forName(DelegateThisUnawareDelegator)
        val delegate = NoMethodsDelegate()

        delegateThisUnawareDelegatorClass.getConstructor(NoMethods::class.java).newInstance(delegate)

        shouldThrow<UninitializedPropertyAccessException> { delegate.delegator }
    }

    @Test
    fun `User class should delegate getName properly`() {
        val userClass = Class.forName(User)
        val setNameMethod = userClass.getMethod("setNameProp", String::class.java)

        val user: NamedObject = userClass
            .getConstructor(String::class.java, Int::class.java)
            .newInstance("Misha", 26) as NamedObject

        user.getName() shouldBe "Misha"

        setNameMethod(user, "Not Misha")

        user.getName() shouldBe "Not Misha"
    }
}

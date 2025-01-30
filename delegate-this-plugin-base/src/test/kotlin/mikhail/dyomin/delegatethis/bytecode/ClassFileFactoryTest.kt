package mikhail.dyomin.delegatethis.bytecode

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.of
import org.junit.jupiter.params.provider.MethodSource
import org.objectweb.asm.ClassReader
import kotlin.test.assertTrue

class ClassFileFactoryTest {
    @ParameterizedTest
    @MethodSource("provide samples")
    fun `class file should supply delegate field descriptors correctly`(
        className: String,
        delegateFieldNames: Set<String>
    ) {
        val factory = ClassFileFactory { name -> ClassReader(name) }
        val classFile = factory.getClassFile(className)
        assertTrue { classFile.delegateFieldDescriptors.keys == delegateFieldNames }
    }

    companion object {
        @JvmStatic
        fun `provide samples`() = listOf(
            of("mikhail.dyomin.delegatethis.samples.ClassWithoutCheckedDelegates", setOf<String>()),
            of("mikhail.dyomin.delegatethis.samples.ClassWithSimpleDelegate", setOf("\$\$delegate_0")),
            of("mikhail.dyomin.delegatethis.samples.ClassWithDeeperDelegate", setOf("namer")),
            of("mikhail.dyomin.delegatethis.samples.ClassWithTiedDelegates", setOf("\$\$delegate_1", "\$\$delegate_2")),
        )
    }
}
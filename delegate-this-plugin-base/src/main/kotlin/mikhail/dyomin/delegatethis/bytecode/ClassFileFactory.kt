package mikhail.dyomin.delegatethis.bytecode

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassReader.*
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

private fun Int.mask(mask: Int): Boolean = (this and mask) == mask

internal class ClassFileFactory(
    private val classReaderFactory: (className: String) -> ClassReader,
) {
    private val delegateClasses: MutableMap<String, Boolean> = HashMap<String, Boolean>().also {
        it["mikhail.dyomin.delegatethis.Delegate"] = true
    }

    private fun classFileFromInternalName(internalName: String): ClassFile = ClassFile(
        internalName.replace(Regex("/"), ".")
    )

    fun getClassFile(className: String) = ClassFile(className)

    internal inner class ClassFile(
        private val className: String
    ) {
        override fun equals(other: Any?) = other is ClassFile && this.className == other.className
        override fun hashCode() = className.hashCode()

        val internalName = className.replace("\\.".toRegex(), "/")

        private val directSupertypes: List<ClassFile> by lazy { readDirectSupertypes() }

        private fun readDirectSupertypes() = object : ClassVisitor(Opcodes.ASM9) {
            val supers = mutableListOf<String>()
            override fun visit(v: Int, a: Int, n: String?, s: String?, superName: String?, interfaces: Array<String>?) {
                supers.addAll(interfaces ?: emptyArray())
                superName?.also { supers.add(it) }
            }
        }.visitBytesIgnoringMethods().supers
            .map { classFileFromInternalName(it) }

        private val isDelegate: Boolean by lazy { checkClassIsDelegate() }

        private fun checkClassIsDelegate(classesBeingChecked: MutableSet<ClassFile> = LinkedHashSet()): Boolean {
            if (!delegateClasses.keys.contains(className)) {
                classesBeingChecked.add(this)
                delegateClasses[className] = (this.directSupertypes - classesBeingChecked).any {
                    it.checkClassIsDelegate(classesBeingChecked)
                }
            }

            return delegateClasses[className]!!
        }

        val delegateFieldDescriptors: Map<String, String> by lazy { readDelegateFieldDescriptors() }

        private fun readDelegateFieldDescriptors() = object : ClassVisitor(Opcodes.ASM9) {
            val fieldDescriptors = mutableMapOf<String, String>()
            override fun visitField(access: Int, name: String?, descriptor: String?, s: String?, v: Any?): Nothing? {
                if (fieldIsDelegate(access, name, descriptor)) {
                    fieldDescriptors[name!!] = descriptor!!
                }
                return null
            }
        }.visitBytesIgnoringMethods().fieldDescriptors

        private fun fieldIsDelegate(access: Int, name: String?, descriptor: String?): Boolean {
            if (descriptor == null || name == null) {
                return false
            }

            val type = Type.getType(descriptor)
            return type.sort == Type.OBJECT &&
                    access.mask(Opcodes.ACC_FINAL) &&
                    !access.mask(Opcodes.ACC_STATIC) &&
                    classFileFromInternalName(type.internalName).isDelegate
        }

        private fun <T : ClassVisitor> T.visitBytesIgnoringMethods(): T = also {
            createReader().accept(it, SKIP_CODE and SKIP_DEBUG and SKIP_FRAMES)
        }

        fun createReader() = classReaderFactory(className)
    }
}

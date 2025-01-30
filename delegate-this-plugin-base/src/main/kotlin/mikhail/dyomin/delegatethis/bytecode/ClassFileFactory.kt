package mikhail.dyomin.delegatethis.bytecode

import org.objectweb.asm.*
import org.objectweb.asm.ClassReader.*

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

        private var _alreadyModified: Boolean? = null
        val alreadyModified: Boolean get() {
            if (null == _alreadyModified) {
                checkModifiedAndGetDelegateFieldDescriptors()
            }
            return _alreadyModified!!
        }

        private var _delegateFieldDescriptors: Map<String, String>? = null
        val delegateFieldDescriptors: Map<String, String> get() {
            if (null == _delegateFieldDescriptors) {
                checkModifiedAndGetDelegateFieldDescriptors()
            }
            return _delegateFieldDescriptors!!
        }

        private fun checkModifiedAndGetDelegateFieldDescriptors() = object: ClassVisitor(Opcodes.ASM9) {
            var modified = false
            val fieldDescriptors = mutableMapOf<String, String>()
            override fun visitAnnotation(descriptor: String?, visible: Boolean) =
                super.visitAnnotation(descriptor, visible).also {
                    modified = (modified || (descriptor == "mikhail/dyomin/delegatethis/Modified"))
                }
            override fun visitField(access: Int, name: String?, descriptor: String?, s: String?, v: Any?): Nothing? {
                if (fieldIsDelegate(access, name, descriptor)) {
                    fieldDescriptors[name!!] = descriptor!!
                }
                return null
            }
        }.visitBytesIgnoringMethods().apply {
            _alreadyModified = modified
            _delegateFieldDescriptors = fieldDescriptors
        }

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

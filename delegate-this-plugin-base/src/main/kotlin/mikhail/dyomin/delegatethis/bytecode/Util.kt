package mikhail.dyomin.delegatethis.bytecode

import org.objectweb.asm.*
import org.objectweb.asm.ClassReader.*

private fun <T : ClassVisitor> T.visitBytesIgnoringMethods(classReader: ClassReader): T = also {
    classReader.accept(it, SKIP_CODE and SKIP_DEBUG and SKIP_FRAMES)
}

fun ClassReader.getAnnotations() = object : ClassVisitor(Opcodes.ASM9) {
    val annotations = mutableSetOf<String>()
    override fun visitAnnotation(descriptor: String?, visible: Boolean) = super.visitAnnotation(descriptor, visible)
        .also { annotations.add(Type.getType(descriptor).className) }
}.visitBytesIgnoringMethods(this).annotations

fun ClassReader.getModifiedBytes() = ClassWriter(this, ClassWriter.COMPUTE_MAXS)
    .also { accept(DelegatorModifierAdapter(it), 0) }
    .toByteArray()!!
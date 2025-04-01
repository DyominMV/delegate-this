package com.github.dyominmv.delegatethis.bytecode

import org.objectweb.asm.*
import org.objectweb.asm.ClassReader.*

data class SimplifiedFieldData(
    val name: String,
    val type: Type,
)

data class SimplifiedClassData(
    val annotationQualifiedNames: List<String>,
    val fields: List<SimplifiedFieldData>,
    val parentsQualifiedNames: List<String>,
)

fun ClassReader.getMetadata() = object : ClassVisitor(Opcodes.ASM9) {
    val annotationQualifiedNames = mutableListOf<String>()
    val fields = mutableListOf<SimplifiedFieldData>()
    val parentsQualifiedNames = mutableListOf<String>()

    private fun internalToQualified(internalName: String) = internalName.replace("/".toRegex(), ".")

    override fun visit(v: Int, a: Int, n: String?, s: String?, superName: String?, interfaces: Array<out String>?) {
        super.visit(v, a, n, s, superName, interfaces)
        superName?.also { parentsQualifiedNames += internalToQualified(it) }
        interfaces?.forEach { parentsQualifiedNames += internalToQualified(it) }
    }

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
        annotationQualifiedNames += Type.getType(descriptor).className
        return super.visitAnnotation(descriptor, visible)
    }

    override fun visitField(access: Int, name: String, descriptor: String, s: String?, v: Any?): FieldVisitor? {
        val type = Type.getType(descriptor)
        if (access and Opcodes.ACC_STATIC == 0 && type.sort == Type.OBJECT) {
            fields += SimplifiedFieldData(name, type)
        }
        return super.visitField(access, name, descriptor, s, v)
    }
}
    .also { this.accept(it, SKIP_CODE and SKIP_DEBUG and SKIP_FRAMES) }
    .run { SimplifiedClassData(annotationQualifiedNames, fields, parentsQualifiedNames) }

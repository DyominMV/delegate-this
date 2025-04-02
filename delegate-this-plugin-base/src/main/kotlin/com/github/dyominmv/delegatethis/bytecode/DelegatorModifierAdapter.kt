package com.github.dyominmv.delegatethis.bytecode

import com.github.dyominmv.delegatethis.AlreadyModified
import com.github.dyominmv.delegatethis.Delegate
import com.github.dyominmv.delegatethis.NonDelegatingConstructorMarker
import org.objectweb.asm.*
import kotlin.math.max
import kotlin.reflect.KClass

private data class ConstructorData(
    val access: Int,
    val descriptor: String,
    val signature: String?,
    val exceptions: Array<out String>?,
)

private class DelegatorModifierAdapter(
    delegateVisitor: ClassVisitor,
    private val fieldsToProvideDelegatorTo: List<SimplifiedFieldData>
) : ClassVisitor(Opcodes.ASM9, delegateVisitor) {
    private lateinit var internalName: String
    private val constructors = mutableListOf<ConstructorData>()

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        val newVersion = max(Opcodes.V1_5, version)
        internalName = name
        super.visit(newVersion, access, name, signature, superName, interfaces)
    }

    // mark all constructors, make marked constructors private
    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        if (name != "<init>") {
            return super.visitMethod(access, name, descriptor, signature, exceptions)
        }

        ConstructorData(
            access = access,
            descriptor = descriptor,
            signature = signature,
            exceptions = exceptions
        ).let { constructors.add(it) }

        val markedDescriptor = addMarkerForDescriptor(descriptor)
        val markedSignature = signature?.let { addMarkerForSignature(it) }
        return constructorCallMarkerVisitor(
            super.visitMethod(Opcodes.ACC_PRIVATE, "<init>", markedDescriptor, markedSignature, exceptions)
        )
    }

    override fun visitEnd() {
        addDelegateThisMethod()
        constructors.forEach { restoreNonMarkedConstructor(it) }
        super.visitAnnotation("L$ALREADY_MODIFIED;", true).apply { visitEnd() }
        super.visitEnd()
    }

    // add Void param to every constructor call
    private fun constructorCallMarkerVisitor(visitor: MethodVisitor) = object : MethodVisitor(Opcodes.ASM9, visitor) {
        override fun visitMethodInsn(
            opcode: Int,
            owner: String?,
            name: String?,
            descriptor: String?,
            isInterface: Boolean
        ) = if (opcode == Opcodes.INVOKESPECIAL && name == "<init>" && owner == internalName) {
            super.visitInsn(Opcodes.ACONST_NULL)
            super.visitMethodInsn(opcode, owner, name, addMarkerForDescriptor(descriptor!!), false)
        } else {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        }
    }

    private fun addDelegateThisMethod() =
        super.visitMethod(Opcodes.ACC_PRIVATE, DELEGATE_THIS, DELEGATE_THIS_DESCRIPTOR, null, emptyArray())
            .apply {
                visitCode()
                fieldsToProvideDelegatorTo.forEach { field ->
                    // this.`delegate_name` ...
                    visitVarInsn(Opcodes.ALOAD, 0)
                    visitFieldInsn(Opcodes.GETFIELD, internalName, field.name, field.type.descriptor)
                    // ... .receiveDelegator(this)
                    visitVarInsn(Opcodes.ALOAD, 0)
                    visitMethodInsn(
                        Opcodes.INVOKEINTERFACE,
                        DELEGATE,
                        RECEIVE_DELEGATOR,
                        "(L${Any::class.internalName};)V",
                        true
                    )
                }
                visitInsn(Opcodes.RETURN)
                visitMaxs(0, 0) // replaced by class writer
                visitEnd()
            }

    private fun restoreNonMarkedConstructor(constructor: ConstructorData) {
        val (access, descriptor, signature, exceptions) = constructor
        val markedDescriptor = addMarkerForDescriptor(descriptor)
        super.visitMethod(access, "<init>", descriptor, signature, exceptions).apply {
            visitCode()
            // call the real constructor
            visitVarInsn(Opcodes.ALOAD, 0)
            moveConstructorParametersToStack(descriptor)
            visitInsn(Opcodes.ACONST_NULL)
            visitMethodInsn(Opcodes.INVOKESPECIAL, internalName, "<init>", markedDescriptor, false)
            // supply `this` to delegates
            visitVarInsn(Opcodes.ALOAD, 0)
            visitMethodInsn(Opcodes.INVOKESPECIAL, internalName, DELEGATE_THIS, DELEGATE_THIS_DESCRIPTOR, false)
            // return
            visitInsn(Opcodes.RETURN)
            visitMaxs(0, 0) // replaced by writer
            visitEnd()
        }
    }

    private fun addMarkerForDescriptor(descriptor: String) =
        descriptor.substring(0, descriptor.lastIndex - 1) + "L${NonDelegatingConstructorMarker::class.internalName};)V"

    private fun addMarkerForSignature(signature: String) =
        signature.replace(")", "L${NonDelegatingConstructorMarker::class.internalName};)")

    private fun MethodVisitor.moveConstructorParametersToStack(descriptor: String) =
        parseConstructorParameters(descriptor).map {
            when (it) {
                'B', 'C', 'I', 'S', 'Z' -> Opcodes.ILOAD
                'J' -> Opcodes.LLOAD
                'F' -> Opcodes.FLOAD
                'D' -> Opcodes.DLOAD
                else -> Opcodes.ALOAD
            }
        }.forEachIndexed { index, instruction -> visitVarInsn(instruction, index + 1) }

    private fun parseConstructorParameters(descriptor: String) = descriptor.substring(1..descriptor.length - 2)
        .let { typePattern.matcher(it).results() }
        .map { it.group()[0] }
        .toList()

    companion object {
        private val typePattern = "(\\[*)B|C|D|F|I|J|S|Z|(L[^;]+;)".toPattern()

        private val KClass<*>.internalName get() = Type.getType(this.java).internalName

        private val ALREADY_MODIFIED = AlreadyModified::class.internalName
        private val DELEGATE = Delegate::class.internalName
        private val RECEIVE_DELEGATOR = Delegate::receiveDelegator.name

        private const val DELEGATE_THIS = "\$delegate_this!"
        private const val DELEGATE_THIS_DESCRIPTOR = "()V"
    }
}

/**
 * applies bytecode modification to the class accessed by [ClassReader].
 * [delegateFields] can be obtained by calling [ClassReader.getMetadata]
 */
fun ClassReader.addDelegatesInitialization(delegateFields: List<SimplifiedFieldData>): ByteArray =
    ClassWriter(this, ClassWriter.COMPUTE_MAXS)
        .also { this.accept(DelegatorModifierAdapter(it, delegateFields), 0) }
        .toByteArray()

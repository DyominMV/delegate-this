package mikhail.dyomin.delegatethis.bytecode

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

private data class ConstructorData(
    val access: Int,
    val descriptor: String,
    val signature: String?,
    val exceptions: Array<out String>?,
)

internal class DelegatorModifierAdapter(
    private val classFile: ClassFileFactory.ClassFile,
    delegateVisitor: ClassVisitor
) : ClassVisitor(Opcodes.ASM9, delegateVisitor) {
    private val constructors = mutableListOf<ConstructorData>()

    // mark all constructors, make marked constructors private
    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        if (name != "<init>") {
            return super.visitMethod(access, name, descriptor, signature, exceptions)
        } else if (descriptor == null) {
            throw IllegalArgumentException("<init> cannot be of null descriptor")
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
        ) = if (opcode == Opcodes.INVOKESPECIAL && name == "<init>" && owner == classFile.internalName) {
            super.visitInsn(Opcodes.ACONST_NULL)
            super.visitMethodInsn(opcode, owner, name, addMarkerForDescriptor(descriptor!!), false)
        } else {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        }
    }

    private fun addDelegateThisMethod() =
        super.visitMethod(Opcodes.ACC_PRIVATE, "\$delegate_this!", "()V", null, emptyArray())
            .apply {
                visitCode()
                classFile.delegateFieldDescriptors.forEach { name, descriptor ->
                    // call this.`delegate_name`.receiveDelegator(this)
                    visitVarInsn(Opcodes.ALOAD, 0)
                    visitFieldInsn(Opcodes.GETFIELD, classFile.internalName, name, descriptor)
                    visitVarInsn(Opcodes.ALOAD, 0)
                    visitMethodInsn(
                        Opcodes.INVOKEINTERFACE,
                        "mikhail/dyomin/delegatethis/Delegate",
                        "receiveDelegator",
                        "(Ljava/lang/Object;)V",
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
        val className = classFile.internalName
        super.visitMethod(access, "<init>", descriptor, signature, exceptions).apply {
            visitCode()
            // call the real constructor
            visitVarInsn(Opcodes.ALOAD, 0)
            moveConstructorParametersToStack(descriptor)
            visitInsn(Opcodes.ACONST_NULL)
            visitMethodInsn(Opcodes.INVOKESPECIAL, className, "<init>", markedDescriptor, false)
            // supply `this` to delegates
            visitVarInsn(Opcodes.ALOAD, 0)
            visitMethodInsn(Opcodes.INVOKESPECIAL, className, "\$delegate_this!", "()V", false)
            // return
            visitInsn(Opcodes.RETURN)
            visitMaxs(0, 0) // replaced by writer
            visitEnd()
        }
    }

    private fun addMarkerForDescriptor(descriptor: String) =
        descriptor.substring(0, descriptor.lastIndex - 1) + "Ljava/lang/Void;)V"

    private fun addMarkerForSignature(signature: String) =
        signature.replace(")", "Ljava/lang/Void;)")

    private fun MethodVisitor.moveConstructorParametersToStack(descriptor: String) {
        var currentDescriptor = descriptor.substring(1, descriptor.lastIndex - 1)
        var varIndex = 1
        while (currentDescriptor != "") {
            when (currentDescriptor[0]) {
                'B', 'C', 'I', 'S', 'Z' -> visitVarInsn(Opcodes.ILOAD, varIndex)
                'J' -> visitVarInsn(Opcodes.LLOAD, varIndex)
                'F' -> visitVarInsn(Opcodes.FLOAD, varIndex)
                'D' -> visitVarInsn(Opcodes.DLOAD, varIndex)
                else -> visitVarInsn(Opcodes.ALOAD, varIndex)
            }
            varIndex += 1
            currentDescriptor = currentDescriptor.replace(typeRegex, "")
        }
    }

    companion object {
        private val typeRegex = run {
            val objectTypeRegex = "L[^;]+;"
            "^(\\[)*(B|C|D|F|I|J|S|Z|($objectTypeRegex))".toRegex()
        }
    }
}
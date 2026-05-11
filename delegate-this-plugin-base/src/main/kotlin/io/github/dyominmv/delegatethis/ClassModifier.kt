package io.github.dyominmv.delegatethis

import java.lang.classfile.Attributes.runtimeVisibleAnnotations
import java.lang.classfile.ClassFile
import java.lang.classfile.ClassModel
import kotlin.jvm.optionals.getOrNull

class ClassModifier(private val delegateCache: DelegateCache) {

    fun modifyClassIfNeeded(classModel: ClassModel, classModificationCallback: (newBytes: ByteArray) -> Unit) {
        if (classModel.isAlreadyModified()) return

        val delegates = classModel.getDelegateFields()
        if (delegates.isEmpty()) return

        val desc = classModel.thisClass().asSymbol()
        val constructors = classModel.methods().filter { it.methodName().equalsString(constructorName) }

        ClassFile.of().transformClass(
            classModel,
            addDelegateThisMethod(delegates)
                    andThen shadowConstructors()
                    andThen callShadowedConstructorsInsteadOfRegular(desc)
                    andThen unshadowConstructorsAndAddCallingDelegateThis(desc, constructors)
                    andThen addAlreadyModifiedAnnotation()
        ).also(classModificationCallback)
    }

    private fun ClassModel.isAlreadyModified() = findAttribute(runtimeVisibleAnnotations())
        .getOrNull()?.annotations()
        ?.any { it.classSymbol() == AlreadyModified::class.desc }
        ?: false

    private fun ClassModel.getDelegateFields() = fields()
        .filter { delegateCache.isDelegate(it.fieldTypeSymbol()) }
}
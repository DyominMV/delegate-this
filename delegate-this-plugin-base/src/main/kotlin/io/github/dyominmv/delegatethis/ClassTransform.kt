package io.github.dyominmv.delegatethis

import java.lang.classfile.Annotation
import java.lang.classfile.ClassTransform
import java.lang.classfile.FieldModel
import java.lang.classfile.MethodModel
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute
import java.lang.classfile.instruction.InvokeInstruction
import java.lang.constant.ClassDesc
import java.lang.reflect.AccessFlag.*

internal const val delegateThisName = $$"$delegate_this!"

internal fun addAlreadyModifiedAnnotation() = ClassTransform { builder, element ->
    if (element is RuntimeVisibleAnnotationsAttribute) {
        val modifiedAnnotations = Annotation.of(AlreadyModified::class.desc)
            .let { RuntimeVisibleAnnotationsAttribute.of(element.annotations() + it) }

        builder.with(modifiedAnnotations)
    } else {
        builder.with(element)
    }
}

internal fun addDelegateThisMethod(delegateFields: List<FieldModel>) = ClassTransform.endHandler {
    it.addMethodBody(delegateThisName, voidMethodTypeDesc(), PRIVATE, FINAL) {
        aload(0)
        delegateFields.forEach { delegate ->
            dup()
            getfield(
                delegate.parent().get().thisClass().asSymbol(),
                delegate.fieldName().stringValue(),
                delegate.fieldTypeSymbol()
            )
            swap()
            dup_x1()
            invokeinterface(
                Delegate::class.desc,
                Delegate::receiveDelegator.name,
                voidMethodTypeDesc(Any::class)
            )
        }
        pop()
        return_()
    }
} as ClassTransform

internal fun shadowConstructors() = ClassTransform { classBuilder, element ->
    if (element !is MethodModel || !element.methodName().equalsString(constructorName)) {
        classBuilder.with(element)
    } else {
        val descriptor = element.methodTypeSymbol().addMarker()
        val flags = element.flags().flags().filter { it != PUBLIC && it != PROTECTED } + PRIVATE

        classBuilder.addMethodBody(constructorName, descriptor, *flags.toTypedArray(), code = element.code().get())
    }
}

internal fun callShadowedConstructorsInsteadOfRegular(
    classDesc: ClassDesc,
) = ClassTransform.transformingMethodBodies { codeBuilder, codeElement ->
    if (
        codeElement !is InvokeInstruction
        || !codeElement.name().equalsString(constructorName)
        || !codeElement.owner().matches(classDesc)
    ) {
        codeBuilder.with(codeElement)
    } else {
        codeBuilder
            .aconst_null()
            .invokespecial(classDesc, constructorName, codeElement.typeSymbol().addMarker())
    }
} as ClassTransform

internal fun unshadowConstructorsAndAddCallingDelegateThis(
    classDesc: ClassDesc,
    originalConstructors: List<MethodModel>,
) = ClassTransform.endHandler { builder ->
    originalConstructors.forEach { constructor ->
        builder.addMethod(constructor.methodName(), constructor.methodType(), constructor.flags().flagsMask()) {
            addEveryElementButCodeOf(constructor)
            addCode {
                // call shadowed constructor
                aload(0)
                constructor.methodTypeSymbol().parameterList().forEachIndexed { index, param ->
                    load(param, index + 1)
                }
                aconst_null()
                invokespecial(classDesc, constructorName, constructor.methodTypeSymbol().addMarker())
                // call delegate_this
                aload(0)
                invokespecial(classDesc, delegateThisName, voidMethodTypeDesc())
                return_()
            }
        }
    }
} as ClassTransform

package io.github.dyominmv.delegatethis

import java.lang.classfile.ClassBuilder
import java.lang.classfile.ClassTransform
import java.lang.classfile.CodeBuilder
import java.lang.classfile.CodeModel
import java.lang.classfile.MethodBuilder
import java.lang.classfile.MethodModel
import java.lang.classfile.constantpool.Utf8Entry
import java.lang.constant.ClassDesc
import java.lang.constant.MethodTypeDesc
import java.lang.reflect.AccessFlag
import kotlin.reflect.KClass

internal const val constructorName = "<init>"

internal fun accessMask(vararg flags: AccessFlag) = flags.map(AccessFlag::mask).reduce(Int::or)

internal val <T: Any> KClass<T>.desc: ClassDesc get() =
    java.describeConstable().orElseThrow { IllegalArgumentException("$this expected to have defined descriptor") }

internal fun methodTypeDesc(returning: KClass<*>, vararg arguments: KClass<*>) =
    MethodTypeDesc.of(returning.desc, arguments.map { it.desc })

internal fun voidMethodTypeDesc(vararg arguments: KClass<*>) =
    MethodTypeDesc.of(ClassDesc.ofDescriptor("V"), arguments.map { it.desc })

internal fun MethodTypeDesc.addMarker() =
    MethodTypeDesc.of(this.returnType(), this.parameterList() + NonDelegatingConstructorMarker::class.desc)

internal infix fun ClassTransform.andThen(other: ClassTransform) = this.andThen(other)

internal fun MethodBuilder.addCode(code: CodeBuilder.() -> Unit) = withCode { it.code() }

internal fun MethodBuilder.addEveryElementButCodeOf(method: MethodModel) =
    method.filter { it !is CodeModel }.forEach { with(it) }

internal fun ClassBuilder.addMethodBody(
    name: String, descriptor: MethodTypeDesc, vararg flags: AccessFlag, code: CodeBuilder.() -> Unit
) = withMethodBody(name, descriptor, accessMask(*flags)) { it.code() }

internal fun ClassBuilder.addMethodBody(
    name: String, descriptor: MethodTypeDesc, vararg flags: AccessFlag, code: CodeModel
) = withMethod(name, descriptor, accessMask(*flags)) { it.with(code) }

internal fun ClassBuilder.addMethod(
    name: Utf8Entry, descriptor: Utf8Entry, flags: Int, method: MethodBuilder.() -> Unit
) = withMethod(name, descriptor, flags) { it.method() }

internal fun CodeBuilder.load(desc: ClassDesc, slot: Int) = when (desc) {
    Byte::class.desc,
    Char::class.desc,
    Int::class.desc,
    Short::class.desc,
    Boolean::class.desc -> iload(slot)

    Long::class.desc -> lload(slot)
    Float::class.desc -> fload(slot)
    Double::class.desc -> dload(slot)
    else -> aload(slot)
}
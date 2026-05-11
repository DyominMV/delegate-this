package io.github.dyominmv.delegatethis

import java.lang.classfile.ClassFile
import java.lang.classfile.ClassModel
import java.lang.constant.ClassDesc
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.walk

class ClassModelProvider(
    private val modifiableClassesRoots: List<Path>,
    private val unmodifiableClassesLoader: ClassLoader,
) {
    private val classFileExtension = "class"

    fun loadClassModel(desc: ClassDesc): ClassModel {
        require(desc.isClassOrInterface) { "$desc expected to be a class or interface descriptor" }

        val internalName = desc.descriptorString().let { it.substring(1, it.length - 1) }
        val classFileName = "$internalName.$classFileExtension"
        val paths = modifiableClassesRoots.map { it.resolve(Path.of(classFileName)) }
            .filter { it.exists() && it.isRegularFile() }

        require(paths.size <= 1) { "expected no more than one file for class $desc but found $paths" }

        return paths.singleOrNull()?.let { ClassFile.of().parse(it) }
            ?: unmodifiableClassesLoader.getResourceAsStream(classFileName)
                ?.let { ClassFile.of().parse(it.readAllBytes()) }
            ?: throw IllegalArgumentException("class $desc not found in modifiable sources nor through classloader")
    }

    fun loadAllModifiableClassModels(): Sequence<Pair<Path, ClassModel>> = modifiableClassesRoots.asSequence()
        .flatMap { it.walk() }
        .filter { it.extension == classFileExtension && it.isRegularFile() && it.exists() }
        .map { it to ClassFile.of().parse(it) }
}
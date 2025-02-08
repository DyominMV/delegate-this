package mikhail.dyomin.delegatethis

import mikhail.dyomin.delegatethis.bytecode.ClassFileFactory
import mikhail.dyomin.delegatethis.bytecode.DelegatorModifierAdapter
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile
import kotlin.io.path.writeBytes

class DelegateThis(
    private val classesRoots: List<Path>
) {
    private val classLoader = this::class.java.classLoader

    private val classFileFactory = ClassFileFactory { className ->
        val classFileName = className
            .replace("\\.".toRegex(), File.separator)
            .replace("<.+$".toRegex(), "")
            .plus(".class")

        val inputStream: InputStream = classFileName
            .let { classesRoots.map { root -> root.resolve(it) } }
            .find { it.exists() && it.isRegularFile() }
            ?.inputStream()
            ?: ClassLoader.getSystemResourceAsStream(classFileName)
            ?: classLoader.getResource(classFileName)?.openStream()
            ?: throw RuntimeException("Could not get resource of class $className")

        ClassReader(inputStream)
    }

    private val classFiles = classesRoots.flatMap { root ->
        root.toFile().walkTopDown()
            .filter { it.isFile && it.extension == "class" }
            .map { it.toPath() }
            .map { path ->
                val className = root.relativize(path)
                    .toString()
                    .replace("\\.class\$".toRegex(), "")
                    .replace(File.separator, ".")

                object {
                    val classFilePath = path
                    val classFile = classFileFactory.getClassFile(className)
                }
            }
    }.associate { it.classFilePath to it.classFile }

    fun execute() = classFiles.forEach { (classFilePath, classFile) ->
        if (!classFile.alreadyModified && classFile.delegateFieldDescriptors.isNotEmpty()) {
            classFilePath.writeBytes(modifyDelegator(classFile))
        }
    }

    private fun modifyDelegator(classFile: ClassFileFactory.ClassFile): ByteArray {
        val reader = classFile.createReader()
        val writer = ClassWriter(reader, ClassWriter.COMPUTE_MAXS)
        val visitor = DelegatorModifierAdapter(classFile, writer)
        reader.accept(visitor, 0)

        return writer.toByteArray()
    }
}

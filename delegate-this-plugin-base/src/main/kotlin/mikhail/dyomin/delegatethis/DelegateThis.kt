package mikhail.dyomin.delegatethis

import mikhail.dyomin.delegatethis.bytecode.ClassFileFactory
import mikhail.dyomin.delegatethis.bytecode.DelegatorModifierAdapter
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File
import java.nio.file.Path
import kotlin.io.path.*

class DelegateThis(
    private val classesRoot: Path
) {
    private val classFileFactory = ClassFileFactory { className ->
        className
            .replace("\\.".toRegex(), File.separator)
            .replace("<.+$".toRegex(), "")
            .plus(".class")
            .let { classesRoot.resolve(it) }
            .takeIf { it.exists() && it.isRegularFile() }
            ?.let { ClassReader(it.inputStream()) }
            ?: ClassReader(className)
    }

    fun execute() = classesRoot.toFile().walkTopDown()
        .filter { it.isFile && it.extension == "class" }
        .map { it.toPath() }
        .forEach { path ->
            val className = path
                .let { classesRoot.relativize(it) }
                .toString()
                .replace("\\.class\$".toRegex(), "")
                .replace(File.separator, ".")

            val classFile = classFileFactory.getClassFile(className)
            if (!classFile.alreadyModified && classFile.delegateFieldDescriptors.isNotEmpty()) {
                path.writeBytes(modifyDelegator(classFile))
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

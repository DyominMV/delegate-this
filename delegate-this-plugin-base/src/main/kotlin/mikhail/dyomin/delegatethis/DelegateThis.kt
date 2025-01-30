package mikhail.dyomin.delegatethis

import mikhail.dyomin.delegatethis.bytecode.ClassFileFactory
import mikhail.dyomin.delegatethis.bytecode.DelegatorModifierAdapter
import org.objectweb.asm.*
import java.io.File
import java.nio.file.Path
import kotlin.io.path.*

class DelegateThis(classesRoot: Path) {
    private val classesRoot = classesRoot.absolute()

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

    fun execute(): Unit = classesRoot.toFile().walkTopDown()
        .filter { it.isFile && it.extension == "class" }
        .map { it.toPath() }
        .forEach { path ->
            val className = path
                .let { classesRoot.relativize(it) }
                .toString()
                .replace("\\.class\$".toRegex(), "")
                .replace(File.separator, ".")

            val classFile = classFileFactory.getClassFile(className)
            if (classFile.delegateFieldDescriptors.isNotEmpty()) {
                path.writeBytes(modifyDelegator(classFile))
            }
        }

    private fun modifyDelegator(classFile: ClassFileFactory.ClassFile): ByteArray {
        val reader = classFile.createReader()
        val writer = ClassWriter(reader, Opcodes.ASM9)
        val visitor = DelegatorModifierAdapter(classFile, writer)
        reader.accept(visitor, 0)

        return writer.toByteArray()
    }
}
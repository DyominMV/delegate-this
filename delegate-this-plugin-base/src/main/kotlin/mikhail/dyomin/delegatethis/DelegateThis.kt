package mikhail.dyomin.delegatethis

import mikhail.dyomin.delegatethis.bytecode.ClassFileFactory
import mikhail.dyomin.delegatethis.bytecode.DelegatorModifierAdapter
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.FileTime
import java.time.Instant
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

    private val lastTransformationDate = classesRoot.resolve(FILE_TO_GET_LAST_TRANSFORMATION_DATE)
        .takeIf { it.exists() && it.isRegularFile() }
        ?.getLastModifiedTime()
        ?: FileTime.from(Instant.MIN)

    fun execute() {
        classesRoot.toFile().walkTopDown()
            .filter {
                it.isFile && it.extension == "class" && it.toPath().getLastModifiedTime() > lastTransformationDate
            }
            .map { it.toPath() }
            .forEach { path ->
                val className = path
                    .let { classesRoot.relativize(it) }
                    .toString()
                    .replace("\\.class\$".toRegex(), "")
                    .replace(File.separator, ".")

                val classFile = classFileFactory.getClassFile(className)
                if (!classFile.alreadyModified || classFile.delegateFieldDescriptors.isNotEmpty()) {
                    path.writeBytes(modifyDelegator(classFile))
                }
            }
        classesRoot.resolve(FILE_TO_GET_LAST_TRANSFORMATION_DATE).writeBytes(byteArrayOf(), StandardOpenOption.CREATE)
    }

    private fun modifyDelegator(classFile: ClassFileFactory.ClassFile): ByteArray {
        val reader = classFile.createReader()
        val writer = ClassWriter(reader, Opcodes.ASM9)
        val visitor = DelegatorModifierAdapter(classFile, writer)
        reader.accept(visitor, 0)

        return writer.toByteArray()
    }

    companion object {
        private const val FILE_TO_GET_LAST_TRANSFORMATION_DATE = "delegators-modified"
    }
}
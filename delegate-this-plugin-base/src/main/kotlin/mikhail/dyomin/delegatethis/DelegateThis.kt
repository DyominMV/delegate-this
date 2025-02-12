package mikhail.dyomin.delegatethis

import mikhail.dyomin.delegatethis.bytecode.getAnnotations
import mikhail.dyomin.delegatethis.bytecode.getModifiedBytes
import org.objectweb.asm.ClassReader
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

    private val classNamesByPaths = classesRoots.flatMap { root ->
        root.toFile().walkTopDown()
            .filter { it.isFile && it.extension == "class" }
            .map { it.toPath() }
            .map { path ->
                val className = root.relativize(path)
                    .toString()
                    .replace("\\.class\$".toRegex(), "")
                    .replace(File.separator, ".")

                object {
                    val className = className
                    val classFilePath = path
                }
            }
    }.associate { it.classFilePath to it.className }

    fun execute() = classNamesByPaths.forEach { (classFilePath, className) ->
        val classReader = getClassReader(className)
        val annotations = classReader.getAnnotations()
        if (!annotations.contains(ALREADY_MODIFIED) && annotations.contains(DELEGATE_THIS)) {
            classFilePath.writeBytes(classReader.getModifiedBytes())
        }
    }

    private fun getClassReader(className: String): ClassReader {
        val classResourceName = className
            .replace("\\.".toRegex(), "/")
            .replace("<.+$".toRegex(), "")
            .plus(".class")

        val inputStream: InputStream = classResourceName
            .replace("/", File.separator)
            .let { classesRoots.map { root -> root.resolve(it) } }
            .find { it.exists() && it.isRegularFile() }
            ?.inputStream()
            ?: ClassLoader.getSystemResource(classResourceName)?.openStream()
            ?: classLoader.getResource(classResourceName)?.openStream()
            ?: throw RuntimeException("Could not get resource of class $className")

        return ClassReader(inputStream)
    }

    companion object {
        private val ALREADY_MODIFIED = AlreadyModified::class.qualifiedName
        private val DELEGATE_THIS = DelegateThis::class.qualifiedName
    }
}

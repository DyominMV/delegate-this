package mikhail.dyomin.delegatethis

import mikhail.dyomin.delegatethis.bytecode.SimplifiedFieldData
import mikhail.dyomin.delegatethis.bytecode.addDelegatesInitialization
import mikhail.dyomin.delegatethis.bytecode.getMetadata
import org.objectweb.asm.ClassReader
import java.io.File
import java.lang.ClassLoader.getSystemClassLoader
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.inputStream
import kotlin.io.path.writeBytes

class DelegateThis(
    modifiableClassesRoots: List<Path>,
    private val unmodifiableClassesLoader: ClassLoader = getSystemClassLoader(),
) {
    private val compiledClassesByNames = modifiableClassesRoots.flatMap { root ->
        root.absolute().toFile().walkTopDown()
            .filter { it.isFile && it.extension == "class" }
            .map { it.toPath() }
            .map { path ->
                val className = root.relativize(path)
                    .toString()
                    .replace("\\.class\$".toRegex(), "")
                    .replace(File.separator, ".")

                className to path
            }
    }.toMap()

    private fun readModifiableClass(path: Path) = ClassReader(path.inputStream())
    private fun readUnmodifiableClass(qualifiedName: String) = ClassReader(
        unmodifiableClassesLoader.getResourceAsStream(qualifiedName.replace('.', '/') + ".class")
    )

    private val delegates = mutableMapOf(Delegate::class.qualifiedName!! to true)
    private val nonDelegateRegex = "^(java|kotlin)(x?)\\..*".toRegex()

    private val metadataCache = compiledClassesByNames.mapValues {
        readModifiableClass(it.value).getMetadata()
    }

    private fun isDelegate(qualifiedName: String): Boolean {
        if (nonDelegateRegex.matches(qualifiedName)) {
            return false
        } else if (delegates.containsKey(qualifiedName)) {
            return delegates[qualifiedName]!!
        }

        val result = getMetadata(qualifiedName)
            .parentsQualifiedNames
            .any { isDelegate(it) }

        delegates[qualifiedName] = result
        return result
    }

    internal fun getMetadata(qualifiedName: String) = try {
        metadataCache[qualifiedName] ?: readUnmodifiableClass(qualifiedName).getMetadata()
    } catch (exception: Throwable) {
        throw IllegalArgumentException("Cannot find class $qualifiedName", exception)
    }

    internal fun List<SimplifiedFieldData>.getDelegatesOnly() = filter { isDelegate(it.type.className) }

    fun execute() = compiledClassesByNames.forEach { (className, path) ->
        val metadata = getMetadata(className)
        val delegateFields = metadata.fields.getDelegatesOnly()
        if (!metadata.annotationQualifiedNames.contains(ALREADY_MODIFIED) && delegateFields.isNotEmpty()) {
            readModifiableClass(path)
                .addDelegatesInitialization(delegateFields)
                .let { path.writeBytes(it) }
        }
    }

    companion object {
        private val ALREADY_MODIFIED = AlreadyModified::class.qualifiedName
    }
}

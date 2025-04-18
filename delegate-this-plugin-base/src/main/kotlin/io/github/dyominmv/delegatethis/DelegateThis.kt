package io.github.dyominmv.delegatethis

import io.github.dyominmv.delegatethis.bytecode.SimplifiedFieldData
import io.github.dyominmv.delegatethis.bytecode.addDelegatesInitialization
import io.github.dyominmv.delegatethis.bytecode.getMetadata
import org.objectweb.asm.ClassReader
import java.io.File
import java.lang.ClassLoader.getSystemClassLoader
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.inputStream
import kotlin.io.path.writeBytes

/**
 * basic class to implement plugin for specified build tool.
 *
 * @param modifiableClassesRoots paths to classes to be modified, usually something like target/classes
 * @param unmodifiableClassesLoader classLoader with access to all dependencies accessible from classes to be modified
 */
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

    private fun readModifiableClass(path: Path) = path.inputStream().use { ClassReader(it) }
    private fun readUnmodifiableClass(qualifiedName: String) =
        unmodifiableClassesLoader.getResourceAsStream(qualifiedName.replace('.', '/') + ".class")
            .use { ClassReader(it) }


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

    /**
     * executes bytecode modification in order to trigger [Delegate.receiveDelegator] on every interface delegate of an
     * object after it is created
     */
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

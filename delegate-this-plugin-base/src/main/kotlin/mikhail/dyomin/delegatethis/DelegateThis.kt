package mikhail.dyomin.delegatethis

import mikhail.dyomin.delegatethis.bytecode.SimplifiedFieldData
import mikhail.dyomin.delegatethis.bytecode.getMetadata
import mikhail.dyomin.delegatethis.bytecode.addDelegatesInitialization
import org.objectweb.asm.ClassReader
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.inputStream
import kotlin.io.path.writeBytes

class DelegateThis(
    classesRoots: List<Path>
) {
    private val compiledClassesByNames = classesRoots.flatMap { root ->
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

    private val delegates = mutableMapOf(Delegate::class.qualifiedName!! to true)
    private val nonDelegateRegex = "^(java|kotlin)(x?)\\..*".toRegex()

    private val metadataCache = compiledClassesByNames.mapValues {
        ClassReader(it.value).getMetadata()
    }

    private fun ClassReader(path: Path) = ClassReader(path.inputStream())

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

    internal fun getMetadata(qualifiedName: String) =
        metadataCache[qualifiedName] ?: ClassReader(qualifiedName).getMetadata()

    internal fun List<SimplifiedFieldData>.getDelegatesOnly() = filter { isDelegate(it.type.className) }

    fun execute() = compiledClassesByNames.forEach { (className, path) ->
        val metadata = getMetadata(className)
        val delegateFields = metadata.fields.getDelegatesOnly()
        if (!metadata.annotationQualifiedNames.contains(ALREADY_MODIFIED) && delegateFields.isNotEmpty()) {
            ClassReader(path)
                .addDelegatesInitialization(delegateFields)
                .let { path.writeBytes(it) }
        }
    }

    companion object {
        private val ALREADY_MODIFIED = AlreadyModified::class.qualifiedName
    }
}

package io.github.dyominmv.delegatethis

import java.lang.ClassLoader.getSystemClassLoader
import java.nio.file.Path
import kotlin.io.path.writeBytes

class DelegateThis(
    modifiableClassesRoots: List<Path>,
    unmodifiableClassesLoader: ClassLoader = getSystemClassLoader(),
) {
    private val classModelProvider = ClassModelProvider(modifiableClassesRoots, unmodifiableClassesLoader)
    private val delegateCache = DelegateCache(classModelProvider)
    private val classModifier = ClassModifier(delegateCache)

    /**
     * executes bytecode modification in order to trigger [Delegate.receiveDelegator] on every interface delegate of an
     * object after it is created
     */
    fun execute() = classModelProvider.loadAllModifiableClassModels().forEach { (path, model) ->
        classModifier.modifyClassIfNeeded(model) { path.writeBytes(it) }
    }
}
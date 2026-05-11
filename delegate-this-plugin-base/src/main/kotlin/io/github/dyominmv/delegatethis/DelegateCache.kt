package io.github.dyominmv.delegatethis

import java.lang.constant.ClassDesc
import kotlin.jvm.optionals.getOrNull

class DelegateCache(
    private val classModelProvider: ClassModelProvider,
    private val nonDelegatePackageRegex: Regex = "^(java|kotlin)x?\\..+$".toRegex(),
) {
    private val delegateClasses = mutableMapOf(
        Delegate::class.java.describeConstable().get() to true,
        Any::class.java.describeConstable().get() to false,
    )

    fun isDelegate(desc: ClassDesc): Boolean {
        if (!desc.isClassOrInterface) return false

        if (nonDelegatePackageRegex.matches(desc.packageName())) return false

        if (!delegateClasses.containsKey(desc)) {
            val model = classModelProvider.loadClassModel(desc)
            val superClasses = listOfNotNull(model.superclass().getOrNull()) + model.interfaces()
            delegateClasses[desc] = superClasses.any { isDelegate(it.asSymbol()) }
        }

        return delegateClasses[desc]!!
    }
}
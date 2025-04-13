package io.github.dyominmv.byproperties

import io.github.dyominmv.delegatethis.Delegate
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * Class that allows delegating methods of [Any] to a list of non-nullable properties. See [properties]
 */
open class Properties<Delegator : Any>(
    protected val delegatorType: KClass<Delegator>,
    protected val properties: List<KProperty1<Delegator, Any>>
) : Delegate, EqualsHashcodeAndToString {
    private lateinit var delegator: Delegator

    /**
     * Checks if properties of delegator are equal to properties of [other]
     */
    override fun equals(other: Any?) = getFromThis() == getFromOther(other)

    /**
     * Gets hashcode based on properties value
     */
    override fun hashCode() = getFromThis().hashCode()

    /**
     * returns string of form
     * ```
     * ClassName([property1]=value1, [property2]=value2...)
     * ```
     */
    override fun toString() = getFromThis().map { "[${it.key}]=${it.value}" }.joinToString(
        prefix = "${delegatorType.simpleName}(",
        separator = ", ",
        postfix = ")"
    )

    @Suppress("UNCHECKED_CAST")
    override fun receiveDelegator(delegator: Any) = delegator.takeIf { delegatorType.isInstance(it) }
        ?.let { this.delegator = it as Delegator }
        ?: throw IllegalArgumentException("delegator should be of type $delegatorType")

    /**
     * @return map of properties current values by their names (for delegator)
     */
    protected fun getFromThis(): Map<String, Any> = properties.associate { it.name to it.get(delegator) }

    /**
     * @return map of properties current values by their names (for [other])
     */
    @Suppress("UNCHECKED_CAST")
    protected fun getFromOther(other: Any?): Map<String, Any>? =
        properties.takeIf { delegatorType.isInstance(other) }
            ?.associate { it.name to it.get(other as Delegator) }

    companion object {
        /**
         * create delegate to specified properties. Note that delegator object should be of type [Delegator] for
         * delegation to work correctly
         */
        inline fun <reified Delegator : Any> properties(vararg properties: KProperty1<Delegator, Any>) =
            Properties(Delegator::class, properties.toList())
    }
}

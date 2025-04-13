package io.github.dyominmv.byproperties

import io.github.dyominmv.byproperties.SingleProperty.Companion.property
import io.github.dyominmv.delegatethis.Delegate
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

sealed class SinglePropertyBase<Delegator : Any, Property>(
    protected val delegatorType: KClass<Delegator>,
    protected val property: KProperty1<Delegator, Property>
) : Delegate {
    /**
     * Reference to delegator, that uses current object as a delegate
     */
    protected lateinit var delegator: Delegator

    @Suppress("UNCHECKED_CAST")
    final override fun receiveDelegator(delegator: Any) {
        require(delegatorType.isInstance(delegator)) { "delegator should be of type $delegatorType" }
        this.delegator = delegator as Delegator
    }

    /**
     * @return property value of [delegator]
     */
    protected fun getFromThis(): Property = property.get(delegator)

    /**
     * @return property value of [other]
     */
    @Suppress("UNCHECKED_CAST")
    protected fun getFromOther(other: Any?) =
        other.takeIf { delegatorType.isInstance(it) }
            ?.let { property.get(it as Delegator) }
}

open class SingleProperty<Delegator : Any, Property : Any>(
    delegatorType: KClass<Delegator>,
    property: KProperty1<Delegator, Property>
) : EqualsHashcodeAndToString, SinglePropertyBase<Delegator, Property>(delegatorType, property) {

    /**
     * checks if [property] value of [delegator] is equal to [property] value of [other]
     */
    override fun equals(other: Any?) = getFromOther(other)?.equals(getFromThis()) ?: false

    /**
     * @return hashCode of [property] value of [delegator]
     */
    override fun hashCode() = getFromThis().hashCode()

    /**
     * @return string of form
     * ```
     * ClassName(property=value)
     * ```
     */
    override fun toString() = "${delegatorType.simpleName}(${property.name}=${getFromThis()})"

    companion object {
        /**
         * create delegate to specified property. Note that delegator object should be of type [Delegator] for
         * delegation to work correctly
         */
        inline fun <reified T : Any, P : Any> property(property: KProperty1<T, P>) = SingleProperty(T::class, property)
    }
}

open class NullableProperty<Delegator : Any, Property>(
    delegatorType: KClass<Delegator>,
    property: KProperty1<Delegator, Property?>
) : EqualsHashcodeAndToString, SinglePropertyBase<Delegator, Property?>(delegatorType, property) {
    private val id get() = System.identityHashCode(delegator)

    /**
     * checks if [property] value of [delegator] is equal to [property] value of [other].
     * If property value of [delegator] is null then check falls back to instance equality check
     */
    override fun equals(other: Any?) = getFromThis()?.equals(getFromOther(other)) ?: (delegator === other)

    /**
     * @return hashCode of [property] value of [delegator]. If property value is null then [delegator]'s identity
     * hashcode is returned
     */
    override fun hashCode() = getFromThis()?.hashCode() ?: id

    /**
     * @return string of form
     * ```
     * ClassName@identityHashCode(property=value)
     * ```
     */
    override fun toString() = "${delegatorType.simpleName}@$id(${property.name}=${getFromThis()})"

    companion object {
        /**
         * create delegate to specified nullable property. Note that delegator object should be of type [Delegator] for
         * delegation to work correctly
         */
        inline fun <reified T : Any, P : Any> nullableProperty(property: KProperty1<T, P?>) =
            NullableProperty(T::class, property)
    }
}

open class ComparedProperty<Delegator : Comparable<Delegator>, Property : Comparable<Property>>(
    delegatorType: KClass<Delegator>,
    property: KProperty1<Delegator, Property>
) : EqualsHashcodeToStringAndComparable<Delegator>, SingleProperty<Delegator, Property>(delegatorType, property) {

    /**
     * compares [delegator]'s property value to [other]'s
     */
    override fun compareTo(other: Delegator) = getFromThis().compareTo(getFromOther(other)!!)

    companion object {
        /**
         * create delegate to specified property. Note that delegator object should be of type [Delegator] for
         * delegation to work correctly
         */
        inline fun <reified T : Comparable<T>, P : Comparable<P>> comparedProperty(property: KProperty1<T, P>) =
            ComparedProperty(T::class, property)
    }
}

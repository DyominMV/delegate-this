package mikhail.dyomin.byproperties

import mikhail.dyomin.delegatethis.Delegate
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

abstract class SinglePropertyBase<Delegator : Any, Property>(
    protected val delegatorType: KClass<Delegator>,
    protected val property: KProperty1<Delegator, Property>
) : Delegate {
    protected lateinit var delegator: Delegator

    @Suppress("UNCHECKED_CAST")
    final override fun receiveDelegator(delegator: Any) = delegator.takeIf { delegatorType.isInstance(it) }
        ?.let { this.delegator = it as Delegator }
        ?: throw IllegalArgumentException("delegator should be of type $delegatorType")

    protected fun getFromThis(): Property = property.get(delegator)

    @Suppress("UNCHECKED_CAST")
    protected fun getFromOther(other: Any?) = other
        ?.takeIf { delegatorType.isInstance(it) }
        ?.let { it as Delegator }
        ?.let { property.get(it) }
}

open class SingleProperty<Delegator : Any, Property : Any>(
    delegatorType: KClass<Delegator>,
    property: KProperty1<Delegator, Property>
) : EqualsHashcodeAndToString, SinglePropertyBase<Delegator, Property>(delegatorType, property) {

    final override fun equals(other: Any?) = getFromOther(other)?.equals(getFromThis()) ?: false

    final override fun hashCode() = getFromThis().hashCode()

    override fun toString() = "${delegatorType.simpleName}(${property.name}=${getFromThis()})"

    companion object {
        inline fun <reified T : Any, P : Any> property(property: KProperty1<T, P>) = SingleProperty(T::class, property)
    }
}

open class NullableProperty<Delegator : Any, Property>(
    delegatorType: KClass<Delegator>,
    property: KProperty1<Delegator, Property?>
) : EqualsHashcodeAndToString, SinglePropertyBase<Delegator, Property?>(delegatorType, property) {
    private fun id() = System.identityHashCode(delegator)

    final override fun equals(other: Any?) =
        if (getFromThis() == null) {
            this === other
        } else {
            getFromThis() == getFromOther(other)
        }

    final override fun hashCode() = getFromThis()?.hashCode() ?: id()

    override fun toString() = "${delegatorType.simpleName}@${id()}(${property.name}=${getFromThis()})"

    companion object {
        inline fun <reified T : Any, P : Any> nullableProperty(property: KProperty1<T, P?>) =
            NullableProperty(T::class, property)
    }
}

abstract class PropertiesBase<Delegator : Any, out Property : Any>(
    protected val delegatorType: KClass<Delegator>,
    protected val properties: List<KProperty1<Delegator, Property>>
) : Delegate {
    protected lateinit var delegator: Delegator

    @Suppress("UNCHECKED_CAST")
    final override fun receiveDelegator(delegator: Any) = delegator.takeIf { delegatorType.isInstance(it) }
        ?.let { this.delegator = it as Delegator }
        ?: throw IllegalArgumentException("delegator should be of type $delegatorType")

    protected fun getFromThis(): Map<String, Property> = properties.associate { it.name to it.get(delegator) }

    @Suppress("UNCHECKED_CAST")
    protected fun getFromOther(other: Any?): Map<String, Property>? {
        if (!delegatorType.isInstance(other)) {
            return null
        }
        return properties.associate { it.name to it.get(other as Delegator) }
    }
}

open class Properties<Delegator : Any, out Property : Any>(
    delegatorType: KClass<Delegator>,
    properties: List<KProperty1<Delegator, Property>>
) : EqualsHashcodeAndToString, PropertiesBase<Delegator, Property>(delegatorType, properties) {
    final override fun equals(other: Any?) = getFromThis() == getFromOther(other)

    final override fun hashCode() = getFromThis().hashCode()

    override fun toString() = getFromThis().map { "[${it.key}]=${it.value}" }.joinToString(
        prefix = "${delegatorType.simpleName}(",
        separator = ", ",
        postfix = ")"
    )

    companion object {
        inline fun <reified Delegator : Any> properties(vararg properties: KProperty1<Delegator, Any>) =
            Properties(Delegator::class, properties.toList())
    }
}

class ComparedProperty<Delegator : Comparable<Delegator>, Property : Comparable<Property>>(
    delegatorType: KClass<Delegator>,
    property: KProperty1<Delegator, Property>
) : EqualsHashcodeToStringAndComparable<Delegator>, SingleProperty<Delegator, Property>(delegatorType, property) {

    override fun compareTo(other: Delegator) = getFromThis().compareTo(getFromOther(other)!!)

    companion object {
        inline fun <reified T : Comparable<T>, P : Comparable<P>> comparedProperty(property: KProperty1<T, P>) =
            ComparedProperty(T::class, property)
    }
}

class ComparedProperties<Delegator : Any, Property : Any>(
    delegatorType: KClass<Delegator>,
    properties: List<KProperty1<Delegator, Property>>
) : EqualsHashcodeToStringAndComparable<Delegator>, Properties<Delegator, Property>(delegatorType, properties) {
    override fun compareTo(other: Delegator) = properties
        .map { (it.get(delegator) as Comparable<Any>).compareTo(it.get(other)) }
        .firstOrNull { it != 0 }
        ?: 0

    companion object {
        inline fun <reified T : Comparable<T>> comparedProperties(vararg properties: KProperty1<T, Comparable<*>>) =
            ComparedProperties(T::class, properties.toList())
    }
}
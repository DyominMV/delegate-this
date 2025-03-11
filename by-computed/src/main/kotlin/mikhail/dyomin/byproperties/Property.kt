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
    final override fun receiveDelegator(delegator: Any) {
        require(delegatorType.isInstance(delegator)) { "delegator should be of type $delegatorType" }
        this.delegator = delegator as Delegator
    }

    protected fun getFromThis(): Property = property.get(delegator)

    @Suppress("UNCHECKED_CAST")
    protected fun getFromOther(other: Any?) =
        property.takeIf { delegatorType.isInstance(it) }
            ?.get(other as Delegator)
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
    private val id get() = System.identityHashCode(delegator)

    final override fun equals(other: Any?) = getFromThis()?.equals(getFromOther(other)) ?: (this === other)

    final override fun hashCode() = getFromThis()?.hashCode() ?: id

    override fun toString() = "${delegatorType.simpleName}@$id(${property.name}=${getFromThis()})"

    companion object {
        inline fun <reified T : Any, P : Any> nullableProperty(property: KProperty1<T, P?>) =
            NullableProperty(T::class, property)
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

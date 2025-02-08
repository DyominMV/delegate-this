package mikhail.dyomin.mavenexample

import mikhail.dyomin.delegatethis.Delegate
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

open class Property<DelegatorType : Any, PropertyType>(
    protected val delegatorType: KClass<DelegatorType>,
    protected val property: KProperty1<DelegatorType, PropertyType>
) : Delegate, EqualsHashcode, ToString {

    private lateinit var delegator: DelegatorType

    protected fun getFromThis() = property.get(delegator)

    @Suppress("UNCHECKED_CAST")
    protected fun getFromOther(other: Any?) = other
        ?.takeIf { delegatorType.isInstance(it) }
        ?.let { it as DelegatorType }
        ?.let { property.get(it) }

    @Suppress("UNCHECKED_CAST")
    final override fun receiveDelegator(delegator: Any) = delegator.takeIf { delegatorType.isInstance(it) }
        ?.let { this.delegator = it as DelegatorType }
        ?: throw IllegalArgumentException("delegator should be of type $delegatorType")

    final override fun equals(other: Any?) = getFromOther(other)?.equals(getFromThis()) ?: false

    final override fun hashCode() = getFromThis().hashCode()

    override fun toString() = "${delegatorType.simpleName}(${property.name}:${getFromThis()})"

    companion object {
        inline fun <reified T : Any, P : Any> property(property: KProperty1<T, P>) = Property(T::class, property)
    }
}

class ComparedProperty<DelegatorType : Comparable<DelegatorType>, PropertyType : Comparable<PropertyType>>(
    delegatorType: KClass<DelegatorType>,
    property: KProperty1<DelegatorType, PropertyType>
) : EqualsHashcodeToStringAndComparable<DelegatorType>, Property<DelegatorType, PropertyType>(delegatorType, property) {
    override fun compareTo(other: DelegatorType) = getFromThis().compareTo(getFromOther(other)!!)

    companion object {
        inline fun <reified T : Comparable<T>, P : Comparable<P>> comparedProperty(property: KProperty1<T, P>) =
            ComparedProperty(T::class, property)
    }
}
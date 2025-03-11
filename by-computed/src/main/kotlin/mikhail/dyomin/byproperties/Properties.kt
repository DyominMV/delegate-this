package mikhail.dyomin.byproperties

import mikhail.dyomin.delegatethis.Delegate
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class Properties<Delegator : Any>(
    private val delegatorType: KClass<Delegator>,
    private val properties: List<KProperty1<Delegator, Any>>
) : Delegate, EqualsHashcodeAndToString {
    private lateinit var delegator: Delegator

    override fun equals(other: Any?) = getFromThis() == getFromOther(other)

    override fun hashCode() = getFromThis().hashCode()

    override fun toString() = getFromThis().map { "[${it.key}]=${it.value}" }.joinToString(
        prefix = "${delegatorType.simpleName}(",
        separator = ", ",
        postfix = ")"
    )

    @Suppress("UNCHECKED_CAST")
    override fun receiveDelegator(delegator: Any) = delegator.takeIf { delegatorType.isInstance(it) }
        ?.let { this.delegator = it as Delegator }
        ?: throw IllegalArgumentException("delegator should be of type $delegatorType")

    private fun getFromThis(): Map<String, Any> = properties.associate { it.name to it.get(delegator) }

    @Suppress("UNCHECKED_CAST")
    private fun getFromOther(other: Any?): Map<String, Any>? =
        properties.takeIf { delegatorType.isInstance(other) }
            ?.associate { it.name to it.get(other as Delegator) }

    companion object {
        inline fun <reified Delegator : Any> properties(vararg properties: KProperty1<Delegator, Any>) =
            Properties(Delegator::class, properties.toList())
    }
}

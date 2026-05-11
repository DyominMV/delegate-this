package io.github.dyominmv.delegatethis.samples

import io.github.dyominmv.delegatethis.Delegate
import kotlin.reflect.KProperty1

interface NamedObject {
    fun getName(): String
}

class NameFromProperty<OwnerClass: Any>(
    private val property: KProperty1<OwnerClass, String>
): Delegate, NamedObject {
    private lateinit var delegator: OwnerClass

    @Suppress("UNCHECKED_CAST")
    override fun receiveDelegator(delegator: Any) {
        this.delegator = delegator as OwnerClass
    }

    override fun getName() = property.get(delegator)
}

data class User(
    var nameProp: String,
    var age: Int,
): NamedObject by NameFromProperty(User::nameProp)
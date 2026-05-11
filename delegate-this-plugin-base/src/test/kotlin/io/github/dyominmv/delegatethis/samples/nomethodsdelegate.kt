package io.github.dyominmv.delegatethis.samples

import io.github.dyominmv.delegatethis.Delegate

interface NoMethods

class NoMethodsDelegate: NoMethods, Delegate {
    lateinit var delegator: Any

    override fun receiveDelegator(delegator: Any) {
        this.delegator = delegator
    }
}

class Delegator(
    val noMethods: NoMethodsDelegate
): NoMethods by noMethods

class ImplicitDelegator(): NoMethods by NoMethodsDelegate()

class DelegateThisUnawareDelegator(
    val noMethods: NoMethods
): NoMethods by noMethods
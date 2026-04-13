package io.github.dyominmv.delegatethis.samples

import io.github.dyominmv.delegatethis.Delegate

class ClassWithSimpleDelegate(
    val dogs: String = "🐕"
) : Comparable<String> by object : Delegate, Comparable<String> {
    lateinit var name: String

    override fun receiveDelegator(delegator: Any) {
        name = delegator.toString()
    }

    override fun compareTo(other: String) = name.compareTo(other)
}
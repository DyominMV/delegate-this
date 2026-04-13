package io.github.dyominmv.delegatethis.samples

import io.github.dyominmv.delegatethis.Delegate

class NameExtractorDelegate: SampleInterface, Delegate {
    private lateinit var name: String
    override fun name(): String = name

    override fun receiveDelegator(delegator: Any) {
        name = delegator.toString()
    }
}
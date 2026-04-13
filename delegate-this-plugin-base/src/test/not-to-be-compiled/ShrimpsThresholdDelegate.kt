package io.github.dyominmv.delegatethis.samples

import io.github.dyominmv.delegatethis.Delegate
import kotlin.math.min

class ShrimpsThresholdDelegate: Shrimper, Delegate {
    var maxShrimpsCount: Int = 0

    override fun takeSomeShrimps(count: Int) = "🦐".repeat(min(maxShrimpsCount, count))
    override fun receiveDelegator(delegator: Any) {
        if (delegator is SampleInterface) {
            maxShrimpsCount = delegator.name().length
        }
    }
}
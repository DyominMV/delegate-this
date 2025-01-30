package mikhail.dyomin.delegatethis.samples

import mikhail.dyomin.delegatethis.Delegate
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
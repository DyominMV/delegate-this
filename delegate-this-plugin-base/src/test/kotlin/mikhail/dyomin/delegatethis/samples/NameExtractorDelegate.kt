package mikhail.dyomin.delegatethis.samples

import mikhail.dyomin.delegatethis.Delegate

class NameExtractorDelegate: SampleInterface, Delegate {
    private lateinit var name: String
    override fun name(): String = name

    override fun receiveDelegator(delegator: Any) {
        name = delegator.toString()
    }
}
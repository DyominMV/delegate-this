package mikhail.dyomin.mavenexample

import mikhail.dyomin.delegatethis.DelegateThis

@DelegateThis
class SampleWithVariable(
    var memes: String
) : EqualsHashcodeAndComparable<SampleWithVariable> by ComparedProperty.comparedProperty(SampleWithVariable::memes),
    ToString by Property.property(SampleWithVariable::id) {
    val id: Int = nextId.also { nextId += 1 }

    companion object {
        private var nextId: Int = 1
    }
}
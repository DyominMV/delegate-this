package mikhail.dyomin.byproperties

interface EqualsHashcode {
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

interface ToString {
    override fun toString(): String
}

interface EqualsHashcodeAndToString: EqualsHashcode, ToString

interface EqualsHashcodeAndComparable<T>: EqualsHashcode, Comparable<T>

interface ToStringAndComparable<T>: ToString, Comparable<T>

interface EqualsHashcodeToStringAndComparable<T>:
    EqualsHashcodeAndComparable<T>,
    ToStringAndComparable<T>,
    EqualsHashcodeAndToString,
    EqualsHashcode,
    ToString,
    Comparable<T>
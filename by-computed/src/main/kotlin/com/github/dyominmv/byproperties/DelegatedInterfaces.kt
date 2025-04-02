package com.github.dyominmv.byproperties

/**
 * interface to allow delegating methods of [Any]. See also [ToString]
 */
interface EqualsHashcode {
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

/**
 * interface to allow delegating methods of [Any]. See also [EqualsHashcode]
 */
interface ToString {
    override fun toString(): String
}

/**
 * interface to allow delegating methods of [Any]. See also [EqualsHashcode], [ToString]
 */
interface EqualsHashcodeAndToString: EqualsHashcode, ToString

/**
 * interface to allow delegating methods of [Any] along with [Comparable].
 * See also [EqualsHashcode], [ToString]
 */
interface EqualsHashcodeAndComparable<T>: EqualsHashcode, Comparable<T>

/**
 * interface to allow delegating methods of [Any] along with [Comparable].
 * See also [EqualsHashcode], [ToString]
 */
interface ToStringAndComparable<T>: ToString, Comparable<T>

/**
 * interface to allow delegating methods of [Any] along with [Comparable].
 * See also [EqualsHashcode], [ToString]
 */
interface EqualsHashcodeToStringAndComparable<T>:
    EqualsHashcodeAndComparable<T>,
    ToStringAndComparable<T>,
    EqualsHashcodeAndToString,
    EqualsHashcode,
    ToString,
    Comparable<T>
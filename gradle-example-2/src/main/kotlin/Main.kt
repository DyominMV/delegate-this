package com.github.dyominmv

import com.github.dyominmv.byproperties.EqualsHashcodeAndToString
import com.github.dyominmv.byproperties.Properties.Companion.properties

class SimpleSample(
    var name: String,
    var age: Int,
) : EqualsHashcodeAndToString by properties(SimpleSample::name, SimpleSample::age)

fun main() {
    val sample = SimpleSample("Boris", 3)
    println(sample)
    sample.age = 100
    println(sample)
}
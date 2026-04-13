package io.github.dyominmv.delegatethis.samples

class ClassWithTiedDelegates(
    private val someList: List<Any>,
):
    Collection<Any> by someList,
    Shrimper by ShrimpsThresholdDelegate(),
    SampleInterface by NameExtractorDelegate()
package mikhail.dyomin.delegatethis.samples

class ClassWithTiedDelegates(
    private val someList: List<Any>,
):
    Collection<Any> by someList,
    Shrimper by ShrimpsThresholdDelegate(),
    SampleInterface by NameExtractorDelegate()
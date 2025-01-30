package mikhail.dyomin.delegatethis.samples

class ClassWithDeeperDelegate(
    val namer: NameExtractorDelegate,
    private var shrimpsCount: Int = "🦐🦐🦐🦐🦐".length / 2
): SampleInterface by namer
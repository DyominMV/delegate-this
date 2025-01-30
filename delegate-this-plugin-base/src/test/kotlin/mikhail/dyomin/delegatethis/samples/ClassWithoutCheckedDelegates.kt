package mikhail.dyomin.delegatethis.samples

class ClassWithoutCheckedDelegates(
    private val name: String,
    val shrimp: String = "🦐",
    val dogs: List<String> = listOf("🐕", "🐕")
): Comparable<String> by "🦐"
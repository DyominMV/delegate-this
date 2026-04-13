# by-computed

Finally, you can delegate interfaces to mutable fields in kotlin

It looks something like this:
```kotlin
class Sample(
var id: Long,
var name: String,
): EqualsHashcode by properties(Sample::id, Sample::name)
```

It works based on the [delegate-this](../delegate-this/readme.md) plugin, so in addition to the dependency, you also
need to connect it.

You can delegate `equals`, `hashCode`, `toString` and `compareTo` to class properties.

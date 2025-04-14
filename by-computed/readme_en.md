# by-computed

Finally, you can delegate interfaces to mutable fields in kotlin
> _you've been waiting for this all your life, I know_

It looks something like this:
```kotlin
class Sample(
var id: Long,
var name: String,
): EqualsHashcode by properties(Sample::id, Sample::name)
```

It works based on the [delegate-this](../delegate-this/readme_en.md) plugin, so in addition to the dependency, you also 
need to connect it.

You can delegate equals, hashcode, toString and compareTo to class properties.

And all classes are open, so you can build as much of your own logic as you want (probably)
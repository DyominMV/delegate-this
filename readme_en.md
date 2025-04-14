# What's in the box?

The popular [by-computed](by-computed/readme_en.md) and its grandfather, the legendary [delegate-this](delegate-this/readme_en.md)

Together they allow you to delegate interface to variable properties:
```kotlin
interface SomeInterface{ /* ... */ }

class Sample(
    var x: SomeInterface
): SomeInterface by property(Sample::x)
```
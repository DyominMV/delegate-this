# Что в коробочке? 

Популярный [by-computed](by-computed/readme.md) и его дед, легендарный [delegate-this](delegate-this/readme.md)

Вместе они позволяют делегировать интерфейс изменяемому свойству:
```kotlin
interface SomeInterface{ /* ... */ }

class Sample(
    var x: SomeInterface
): SomeInterface by property(Sample::x)
```

[same text in English](readme_en.md)
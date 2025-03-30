# by-computed

Наконец-то в котлине можно делегировать интерфейсы изменяемым полям
> _вы ждали этого всю жизнь, я знаю_

Выглядит это как-то так:
```kotlin
class Sample(
    var id: Long,
    var name: String,
): EqualsHashcode by properties(Sample::id, Sample::name)
```

Работает на основе плагина [delegate-this](../delegate-this/readme.md), так что кроме зависимости, нужно подключить ещё
и его.

Ещё свойствам класса можно делегировать equals, hashcode, toString и compareTo.

Ну и все классы открытые, так что можно городить сколько угодно своей логики

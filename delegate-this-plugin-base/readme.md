# База для плагина delegate-this

Трансформация классов лежит тут. Чтобы адаптировать плагин к системе сборки, нужно добавить в систему сборки действие, 
которое бы выполняло следующий код:
```kotlin
DelegateThis(
    modifiableClassRoots = TODO("Пути к папкам с файлами .class, например build/classes или target/classes"),
    unmodifiableClassesLoader = TODO("Класслоадер с доступом к классам в зависимостях проекта")
).execute()
```
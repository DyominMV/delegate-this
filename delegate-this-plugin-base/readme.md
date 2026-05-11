# Base for delegate-this plugin

Class transformation happens here. To use it within your build script run following:
```kotlin
DelegateThis(
    modifiableClassRoots = TODO("Paths to folders with .class files, such as build/classes or target/classes"),
    unmodifiableClassesLoader = TODO("Classloader with access to classes in project dependencies")
).execute()
```
# Base for delegate-this plugin

Class transformation happens here. To adapt the plugin to the build system, you need to add an action to the build system,
which would execute the following code:
```kotlin
DelegateThis(
    modifiableClassRoots = TODO("Paths to folders with .class files, such as build/classes or target/classes"),
    unmodifiableClassesLoader = TODO("Classloader with access to classes in project dependencies")
).execute()
```
# delegate-this-gradle-plugin

This plugin contains and connects tasks for modifying the bytecode of classes in kotlin so that the trick with
[delegate-this](../delegate-this/readme.md) works.

Plugin adds tasks `transformDelegators` and `transformTestDelegators` of type `TransformDelegators`. The tasks perform 
bytecode transformations that add call to `Delegate#receiveDelegator` at the end of delegator constructor code 
execution.

usage:

1. add following to `settings.gradle.kts`, so that your project can use plugins from maven central  
   ```kotlin
   pluginManagement {
       repositories {
           gradlePluginPortal()
           mavenCentral()
       }
   }
   ```
2. add following to build.gradle.kts
   ```kotlin
   plugins {
       id("io.github.dyominmv.delegate-this-gradle-plugin") version "1.1.1"
   }
   ```

For example of using the plugin see [example](../gradle-example/build.gradle.kts)

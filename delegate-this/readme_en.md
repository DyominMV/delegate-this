# delegate-this

Finally in Kotlin, an interface delegate can use a reference to the delegater!

> _you've been waiting for this all your life, I know_

## Introduction

### Why it's bad

Actually, when a delegate is created, the construction of `this` is not yet complete. Most likely, this is why kotlin
initially does not even allow using `this` in the expression after the `by` word. Moreover, even with such a wonderfully 
safe approach as in this plugin, you can catch surprising effects if you try to delegate several interfaces
to several different delegates, and for one of them rely on the correct implementation of the other.

### Why it's good

Actually, it can be useful to delegate certain logic differently depending on the state of the object. And if this
logic is described by a fairly large interface, then it will be unpleasant to write the same `if`s in each of its 
methods.

> _Using a proxy for such purposes is even worse than allowing a link to the delegator_

## Usage

### 1. Plugins

You won't be able to just get a link to the delegator, you'll have to add a little bytecode transformation magic first.
For this, the `delegate-this-(maven|gradle)-plugin` plugins are implemented:

- maven:
 ```xml
 <plugins>
     <plugin>
        <groupId>io.github.dyominmv</groupId>
            <artifactId>delegate-this-maven-plugin</artifactId>
            <version>${current version}</version>
            <executions>
                <execution>
                    <id>compile</id>
                    <phase>compile</phase>
                    <goals><goal>transform-delegators</goal></goals>
                </execution>
                <execution>
                    <id>test-compile</id>
                    <phase>test-compile</phase>
                    <goals><goal>transform-test-delegators</goal></goals>
                </execution>
            </executions>
        </plugin>
 </plugins>
```
- gradle:
```kts
plugins {
    id("io.github.dyominmv.delegate-this-gradle-plugin") version "current version"
}
```

### 2. Dependencies

If you want to get a reference to the delegator in your delegate, then in addition to connecting plugins, you will need 
to add a dependency on `delegate-this`.

- maven:
```xml
<dependency>
    <groupId>io.github.dyominmv</groupId>
    <artifactId>delegate-this</artifactId>
    <version>${current version}</version>
</dependency>
```
- gradle:
```kts
implementation("io.github.dyominmv", "delegate-this", "current version")
```

### 3. Get a reference to the delegator inside the delegate

The expression that initializes the delegate must return an implementation of the Delegate interface.
For example, like this:

```kotlin
interface Animal {
    fun makeSound()
}

class Cat : Animal, Delegate {
    private lateinit var delegator: Any
    override fun receiveDelegator(delegator: Any) {
        this.delegator = delegator
    }

    override fun makeSound() = println("Meow, my hashCode is ${delegator.hashCode()}")
}

class AnimalDelegator: Animal by Cat()
```

## How does it work?

The essence of the transformation is as follows:
1. Among the just compiled class files, those are found that have fields that are descendants of the Delegate type. 
    Further we deal only with those classes that have such fields.
2. A private method `delegate_this!()` is added to the class, which passes `this` to each delegate field (by calling the
    `receiveDelegator` method)
3. Each class constructor is made private and an empty marker parameter is added to it (accordingly, the calls to these 
    constructors inside the modified constructors are replaced, _well, you get the idea_)
4. Constructors with the original signature are returned, but inside they first call their modified clone, and
   then they call `delegate_this!()`

All this is done so that the `receiveDelegator` method is called only once, immediately after the logic of constructing 
the delegate object.

## Initial reason for doing all that

The ability to delegate interfaces to mutable properties. The [by-computed](../by-computed/readme_en.md) project is 
exactly about this.
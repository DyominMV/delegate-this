This repository contains kotlin library and plugins to add the following abilities:

## Access the delegating object reference from the delegate object with [delegate-this](delegate-this)
  ```kotlin
  class Sample: SomeInterface by SomeDelegate()

  class SomeDelegate: SomeInterface, Delegate {
      override fun receiveDelegator(delegator: Any) {
          // here delegator is instance of Sample or other class 
          // that delegates to SomeDelegate 
      } 
  }
  ```

## Delegation of interface to a variable property via [by-computed](by-computed)
  ```kotlin
  interface SomeInterface{ /* ... */ }

  class Sample(var x: SomeInterface): SomeInterface by property(Sample::x)
  ```
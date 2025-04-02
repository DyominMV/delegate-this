package com.github.dyominmv.delegatethis

/**
 * Should be implemented in order to get reference to delegating object.
 */
interface Delegate {
    /**
     * Passes delegator reference to the delegate. This method should be called only once during delegating object
     * lifecycle. Normally is call is added to constructor bytecode by plugin.
     */
    fun receiveDelegator(delegator: Any)
}
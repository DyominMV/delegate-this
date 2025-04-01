package com.github.dyominmv.bycomputed

import com.github.dyominmv.delegatethis.Delegate
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy.newProxyInstance
import kotlin.reflect.KClass

private class DelegatingInvocationHandler<Delegator : Any, TargetInterface : Any>(
    private val getter: Delegator.() -> TargetInterface
) : InvocationHandler {
    private lateinit var delegator: Delegator

    @Suppress("UNCHECKED_CAST")
    override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any = when (method) {
        receiveDelegator -> delegator = args!![0] as Delegator
        else -> method.invoke(delegator.getter(), *(args ?: arrayOf()))
    }

    companion object {
        private val receiveDelegator =
            Delegate::class.java.getDeclaredMethod(Delegate::receiveDelegator.name, Any::class.java)
    }
}

fun <Delegator : Any, TargetInterface : Any> delegatingProxy(
    loader: ClassLoader,
    targetInterface: KClass<TargetInterface>,
    getter: Delegator.() -> TargetInterface
): TargetInterface {
    require(targetInterface.java.isInterface) { "targetInterface is required to be interface" }
    require(!targetInterface.java.isSealed) { "targetInterface is required to be non-sealed" }

    val interfaces = arrayOf(Delegate::class.java, targetInterface.java)
    @Suppress("UNCHECKED_CAST")
    return newProxyInstance(loader, interfaces, DelegatingInvocationHandler(getter)) as TargetInterface
}

class ComputedDelegate<DelegatedInterface : Any>(val delegatedInterface: KClass<DelegatedInterface>) {
    inline fun <reified Delegator : Any> to(noinline getter: Delegator.() -> DelegatedInterface) =
        delegatingProxy(Delegator::class.java.classLoader, delegatedInterface, getter)
}

inline fun <reified DelegatedInterface : Any> delegating() = ComputedDelegate(DelegatedInterface::class)

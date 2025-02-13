package mikhail.dyomin.bycomputed

import mikhail.dyomin.delegatethis.Delegate
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy.newProxyInstance

class DelegatingInvocationHandler<Delegator : Any, TargetInterface : Any>(
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

class ComputedDelegate<DelegatedInterface : Any>(val delegatedInterface: Class<DelegatedInterface>) {
    inline fun <reified Delegator : Any> of(noinline getter: Delegator.() -> DelegatedInterface): DelegatedInterface {
        val loader = Delegator::class.java.classLoader
        val interfaces = arrayOf(Delegate::class.java, delegatedInterface)

        @Suppress("UNCHECKED_CAST")
        return newProxyInstance(loader, interfaces, DelegatingInvocationHandler(getter)) as DelegatedInterface
    }
}

inline fun <reified DelegatedInterface : Any> computed(): ComputedDelegate<DelegatedInterface> {
    val delegatedInterface = DelegatedInterface::class.java
    if (!delegatedInterface.isInterface || delegatedInterface.isSealed) {
        throw IllegalArgumentException("$delegatedInterface should be non-sealed interface")
    }
    return ComputedDelegate(delegatedInterface)
}

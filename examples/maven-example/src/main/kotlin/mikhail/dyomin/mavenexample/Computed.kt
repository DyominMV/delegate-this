package mikhail.dyomin.mavenexample

import mikhail.dyomin.delegatethis.Delegate
import mikhail.dyomin.delegatethis.DelegateThis
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class ComputedDelegate<Delegator : Any, Computed : Any>(
    val classLoader: ClassLoader,
    val getter: Delegator.() -> Computed
) {
    inline fun <reified TargetInterface> asDelegate(): TargetInterface {
        if (!TargetInterface::class.java.isInterface) {
            throw RuntimeException()
        }
        return Proxy.newProxyInstance(
            classLoader,
            arrayOf(Delegate::class.java, TargetInterface::class.java),
            @Suppress("UNCHECKED_CAST")
            object : InvocationHandler {
                lateinit var delegator: Delegator
                override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?) =
                    if (method == receiveDelegator) {
                        delegator = args!![0] as Delegator
                    } else {
                        method.invoke(delegator.getter(), *(args ?: arrayOf<Any>()))
                    }
            }
        ) as TargetInterface
    }

    inline operator fun <reified TargetInterface> invoke() = asDelegate<TargetInterface>()
}

val receiveDelegator: Method =
    Delegate::class.java.getDeclaredMethod(Delegate::receiveDelegator.name, Any::class.java)

inline fun <reified Delegator : Any, reified Variable : Any> computed(noinline getter: Delegator.() -> Variable) =
    ComputedDelegate(Delegator::class.java.classLoader, getter)

@DelegateThis
class Shrimper(
    private val representation: String,
    var count: Int
) : Comparable<String> by computed(Shrimper::count)()


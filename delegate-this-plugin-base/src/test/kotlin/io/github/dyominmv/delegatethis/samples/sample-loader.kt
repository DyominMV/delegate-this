package io.github.dyominmv.delegatethis.samples

import java.lang.invoke.MethodHandles

private val lookup = MethodHandles.lookup()
fun loadClass(bytes: ByteArray) = lookup.defineClass(bytes)
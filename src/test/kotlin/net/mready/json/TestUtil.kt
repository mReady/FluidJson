package net.mready.json

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

const val PATH_ROOT = "[root]"

inline fun assertFailsOn(vararg path: String, block: () -> Unit) {
    val e = assertFailsWith(JsonValueException::class, null, block)
    assertEquals(path.joinToString(" > "), e.path.toString())
}

inline fun assetSucceeds(block: () -> Unit) {
    try {
        block()
    } catch (e: Throwable) {
        throw AssertionError("Should complete successfully", e)
    }
}
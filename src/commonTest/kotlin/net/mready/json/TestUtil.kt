package net.mready.json

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.asserter

const val PATH_ROOT = "[json root]"

inline fun assertFailsOn(vararg path: String, block: () -> Unit) {
    val e = assertFailsWith(FluidJsonException::class, null, block)
    assertEquals(path.joinToString(" > "), e.path.toString())
}

inline fun assetSucceeds(block: () -> Unit) {
    try {
        block()
    } catch (e: Throwable) {
        asserter.fail("Should complete successfully", e)
    }
}
package net.mready.json

import net.mready.json.adapters.KotlinxJsonAdapter
import net.mready.json.internal.JsonEmptyElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

open class JsonEmptyTests {
    open val adapter: JsonAdapter = KotlinxJsonAdapter()

    @Test
    fun emptyIsNull() {
        val json = FluidJson()

        assertTrue { json is JsonEmptyElement }
        assertEquals(true, json.isNull)
        assertEquals(null, json.orNull)

        assertEquals(0, json.size)

        assertEquals(null, json.stringOrNull)
        assertFailsOn("$") { json.string }

        assertEquals(null, json.intOrNull)
        assertFailsOn("$") { json.int }

        assertEquals(null, json.longOrNull)
        assertFailsOn("$") { json.long }

        assertEquals(null, json.doubleOrNull)
        assertFailsOn("$") { json.double }

        assertEquals(null, json.boolOrNull)
        assertFailsOn("$") { json.bool }
    }

    @Test
    fun emptyRemainsNullAfterNestedAccess() {
        val jsonObj = FluidJson()

        assertEquals(true, jsonObj["a"]["b"].isNull)
        assertEquals(null, jsonObj["a"]["b"].orNull)
        assertEquals(null, jsonObj["a"]["b"].objOrNull)
        // should fail on "$" but it's difficult to fix right now
        assertFailsOn("$['a']") { jsonObj["a"]["b"].obj }

        val jsonArr = FluidJson()
        assertEquals(true, jsonArr[0][1].isNull)
        assertEquals(null, jsonArr[0][1].orNull)
        assertEquals(null, jsonArr[0][1].arrayOrNull)
        // should fail on "$" but it's difficult to fix right now
        assertFailsOn("$[0]") { jsonArr[0][1].array }
    }
}
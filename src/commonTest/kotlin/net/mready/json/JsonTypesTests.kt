package net.mready.json

import net.mready.json.internal.*
import net.mready.json.adapters.KotlinxJsonAdapter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

open class JsonTypesTests {
    open val adapter: JsonAdapter = KotlinxJsonAdapter()

    @Test
    fun emptyJson() {
        val json = FluidJson()

        assertTrue { json is JsonEmptyElement }
        assertEquals(true, json.isNull)
        assertEquals(null, json.orNull)

        assertEquals(0, json.size)

        assertEquals(null, json.stringOrNull)
        assertFailsOn(PATH_ROOT) { json.string }

        assertEquals(null, json.intOrNull)
        assertFailsOn(PATH_ROOT) { json.int }

        assertEquals(null, json.longOrNull)
        assertFailsOn(PATH_ROOT) { json.long }

        assertEquals(null, json.doubleOrNull)
        assertFailsOn(PATH_ROOT) { json.double }

        assertEquals(null, json.boolOrNull)
        assertFailsOn(PATH_ROOT) { json.bool }
    }

    @Test
    fun nullJson() {
        val json = FluidJson.wrap(null)

        assertTrue { json is JsonNullElement }
        assertEquals(true, json.isNull)
        assertEquals(null, json.orNull)

        assertFailsOn(PATH_ROOT)  { json.size }
        assertTrue { json["test"] is JsonErrorElement }
        assertTrue { json[0] is JsonErrorElement }

        assertEquals(null, json.stringOrNull)
        assertFailsOn(PATH_ROOT) { json.string }

        assertEquals(null, json.intOrNull)
        assertFailsOn(PATH_ROOT) { json.int }

        assertEquals(null, json.longOrNull)
        assertFailsOn(PATH_ROOT) { json.long }

        assertEquals(null, json.doubleOrNull)
        assertFailsOn(PATH_ROOT) { json.double }

        assertEquals(null, json.boolOrNull)
        assertFailsOn(PATH_ROOT) { json.bool }
    }

    @Test
    fun errorJson() {
        val json: FluidJson = JsonErrorElement(FluidJsonException("test", JsonPath.ROOT), adapter = adapter)

        assertTrue { json is JsonErrorElement }
        assertEquals(true, json.isNull)
        assertEquals(null, json.orNull)

        assertFailsOn(PATH_ROOT)  { json.size }
        assertTrue { json["test"] is JsonErrorElement }
        assertTrue { json[0] is JsonErrorElement }

        assertEquals(null, json.stringOrNull)
        assertFailsOn(PATH_ROOT) { json.string }

        assertEquals(null, json.intOrNull)
        assertFailsOn(PATH_ROOT) { json.int }

        assertEquals(null, json.longOrNull)
        assertFailsOn(PATH_ROOT) { json.long }

        assertEquals(null, json.doubleOrNull)
        assertFailsOn(PATH_ROOT) { json.double }

        assertEquals(null, json.boolOrNull)
        assertFailsOn(PATH_ROOT) { json.bool }
    }

    @Test
    fun stringJson() {
        val string = "test"
        val json = FluidJson.wrap(string)

        assertTrue { json is JsonPrimitiveElement }
        assertEquals(false, json.isNull)
        assertEquals(json, json.orNull)

        assertFailsOn(PATH_ROOT)  { json.size }
        assertTrue { json["test"] is JsonErrorElement }
        assertTrue { json[0] is JsonErrorElement }

        assertEquals(string, json.stringOrNull)
        assertEquals(string, json.string)

        assertEquals(null, json.intOrNull)
        assertFailsOn(PATH_ROOT) { json.int }

        assertEquals(null, json.longOrNull)
        assertFailsOn(PATH_ROOT) { json.long }

        assertEquals(null, json.doubleOrNull)
        assertFailsOn(PATH_ROOT) { json.double }

        assertEquals(null, json.boolOrNull)
        assertFailsOn(PATH_ROOT) { json.bool }
    }

    @Test
    fun intJson() {
        val number = 1
        val json = FluidJson.wrap(number)

        assertTrue { json is JsonPrimitiveElement }
        assertEquals(false, json.isNull)
        assertEquals(json, json.orNull)

        assertFailsOn(PATH_ROOT)  { json.size }
        assertTrue { json["test"] is JsonErrorElement }
        assertTrue { json[0] is JsonErrorElement }

        assertEquals(null, json.stringOrNull)
        assertFailsOn(PATH_ROOT) { json.string }

        assertEquals(1, json.intOrNull)
        assertEquals(1, json.int)

        assertEquals(1L, json.longOrNull)
        assertEquals(1L, json.long)

        assertEquals(1.0, json.doubleOrNull)
        assertEquals(1.0, json.double)

        assertEquals(null, json.boolOrNull)
        assertFailsOn(PATH_ROOT) { json.bool }
    }

    @Test
    fun longJson() {
        val number = 1L
        val json = FluidJson.wrap(number)

        assertTrue { json is JsonPrimitiveElement }
        assertEquals(false, json.isNull)
        assertEquals(json, json.orNull)

        assertFailsOn(PATH_ROOT)  { json.size }
        assertTrue { json["test"] is JsonErrorElement }
        assertTrue { json[0] is JsonErrorElement }

        assertEquals(null, json.stringOrNull)
        assertFailsOn(PATH_ROOT) { json.string }

        assertEquals(1, json.intOrNull)
        assertEquals(1, json.int)

        assertEquals(1L, json.longOrNull)
        assertEquals(1L, json.long)

        assertEquals(1.0, json.doubleOrNull)
        assertEquals(1.0, json.double)

        assertEquals(null, json.boolOrNull)
        assertFailsOn(PATH_ROOT) { json.bool }
    }

    @Test
    fun doubleJson() {
        val number = 1.0
        val json = FluidJson.wrap(number)

        assertTrue { json is JsonPrimitiveElement }
        assertEquals(false, json.isNull)
        assertEquals(json, json.orNull)

        assertFailsOn(PATH_ROOT)  { json.size }
        assertTrue { json["test"] is JsonErrorElement }
        assertTrue { json[0] is JsonErrorElement }

        assertEquals(null, json.stringOrNull)
        assertFailsOn(PATH_ROOT) { json.string }

        assertEquals(null, json.intOrNull)
        assertFailsOn(PATH_ROOT) { json.int }

        assertEquals(null, json.longOrNull)
        assertFailsOn(PATH_ROOT) { json.long }

        assertEquals(1.0, json.doubleOrNull)
        assertEquals(1.0, json.double)

        assertEquals(null, json.boolOrNull)
        assertFailsOn(PATH_ROOT) { json.bool }
    }

    @Test
    fun jsonObject() {
        val json = jsonObject {
        }

        assertTrue { json is JsonObjectElement }
        assertEquals(false, json.isNull)
        assertEquals(json, json.orNull)

        assertEquals(0, json.size)
        assertTrue { json["test"] is JsonEmptyElement }
        assertTrue { json[0] is JsonErrorElement }

        assertEquals(null, json.stringOrNull)
        assertFailsOn(PATH_ROOT) { json.string }

        assertEquals(null, json.intOrNull)
        assertFailsOn(PATH_ROOT) { json.int }

        assertEquals(null, json.longOrNull)
        assertFailsOn(PATH_ROOT) { json.long }

        assertEquals(null, json.doubleOrNull)
        assertFailsOn(PATH_ROOT) { json.double }

        assertEquals(null, json.boolOrNull)
        assertFailsOn(PATH_ROOT) { json.bool }
    }

    @Test
    fun jsonArray() {
        val json = jsonArray {
        }

        assertTrue { json is JsonArrayElement }
        assertEquals(false, json.isNull)
        assertEquals(json, json.orNull)

        assertEquals(0, json.size)
        assertTrue { json["test"] is JsonErrorElement }
        assertTrue { json[0] is JsonEmptyElement }

        assertEquals(null, json.stringOrNull)
        assertFailsOn(PATH_ROOT) { json.string }

        assertEquals(null, json.intOrNull)
        assertFailsOn(PATH_ROOT) { json.int }

        assertEquals(null, json.longOrNull)
        assertFailsOn(PATH_ROOT) { json.long }

        assertEquals(null, json.doubleOrNull)
        assertFailsOn(PATH_ROOT) { json.double }

        assertEquals(null, json.boolOrNull)
        assertFailsOn(PATH_ROOT) { json.bool }
    }
}
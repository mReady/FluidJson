package net.mready.json

import net.mready.json.kotlinx.KotlinxJsonAdapter
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JsonTypesTests {
    private val adapter: JsonAdapter = KotlinxJsonAdapter()

    @Test
    fun emptyJson() {
        val json = JsonValue()

        assertTrue { json is JsonEmpty }
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
        val json = JsonValue(null)

        assertTrue { json is JsonNull }
        assertEquals(true, json.isNull)
        assertEquals(null, json.orNull)

        assertFailsOn(PATH_ROOT)  { json.size }
        assertTrue { json["test"] is JsonError }
        assertTrue { json[0] is JsonError }

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
        val json: JsonValue = JsonError(JsonValueException("test", JsonPath.ROOT))

        assertTrue { json is JsonError }
        assertEquals(true, json.isNull)
        assertEquals(null, json.orNull)

        assertFailsOn(PATH_ROOT)  { json.size }
        assertTrue { json["test"] is JsonError }
        assertTrue { json[0] is JsonError }

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
        val json = JsonValue(string)

        assertTrue { json is JsonPrimitive }
        assertEquals(false, json.isNull)
        assertEquals(json, json.orNull)

        assertFailsOn(PATH_ROOT)  { json.size }
        assertTrue { json["test"] is JsonError }
        assertTrue { json[0] is JsonError }

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
        val json = JsonValue(number)

        assertTrue { json is JsonPrimitive }
        assertEquals(false, json.isNull)
        assertEquals(json, json.orNull)

        assertFailsOn(PATH_ROOT)  { json.size }
        assertTrue { json["test"] is JsonError }
        assertTrue { json[0] is JsonError }

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
        val json = JsonValue(number)

        assertTrue { json is JsonPrimitive }
        assertEquals(false, json.isNull)
        assertEquals(json, json.orNull)

        assertFailsOn(PATH_ROOT)  { json.size }
        assertTrue { json["test"] is JsonError }
        assertTrue { json[0] is JsonError }

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
        val json = JsonValue(number)

        assertTrue { json is JsonPrimitive }
        assertEquals(false, json.isNull)
        assertEquals(json, json.orNull)

        assertFailsOn(PATH_ROOT)  { json.size }
        assertTrue { json["test"] is JsonError }
        assertTrue { json[0] is JsonError }

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

        assertTrue { json is JsonObject }
        assertEquals(false, json.isNull)
        assertEquals(json, json.orNull)

        assertEquals(0, json.size)
        assertTrue { json["test"] is JsonEmpty }
        assertTrue { json[0] is JsonError }

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

        assertTrue { json is JsonArray }
        assertEquals(false, json.isNull)
        assertEquals(json, json.orNull)

        assertEquals(0, json.size)
        assertTrue { json["test"] is JsonError }
        assertTrue { json[0] is JsonEmpty }

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
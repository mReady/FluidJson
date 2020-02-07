package net.mready.json

import net.mready.json.kotlinx.KotlinxJsonAdapter
import org.intellij.lang.annotations.Language
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue


class ParseTests {
    private val adapter: JsonAdapter = KotlinxJsonAdapter()

    @Test
    fun emptyString() {
        val json = adapter.parse("")

        assertTrue { json is JsonEmpty }
    }

    @Test
    fun plainString() {
        val json = adapter.parse("hello")

        assertTrue { json is JsonPrimitive }
        assertTrue { (json as JsonPrimitive).type == JsonPrimitive.Type.UNKNOWN }
        assertEquals("hello", json.string)
    }

    @Test
    fun quotedString() {
        val json = adapter.parse("\"hello\"")

        assertTrue { json is JsonPrimitive }
        assertTrue { (json as JsonPrimitive).type == JsonPrimitive.Type.UNKNOWN }
        assertEquals("\"hello\"", json.string)
    }

    @Test
    fun plainInt() {
        val json = adapter.parse("123")

        assertTrue { json is JsonPrimitive }
        assertTrue { (json as JsonPrimitive).type == JsonPrimitive.Type.UNKNOWN }
        assertEquals("123", json.string)
        assertEquals(123, json.int)
        assertEquals(123L, json.long)
    }

    @Test
    fun plainDouble() {
        val json = adapter.parse("123.0")

        assertTrue { json is JsonPrimitive }
        assertTrue { (json as JsonPrimitive).type == JsonPrimitive.Type.UNKNOWN }
        assertEquals("123.0", json.string)
        assertEquals(123.0, json.double)
    }

    @Test
    fun invalidDouble() {
        val json = adapter.parse("123.0.0")

        assertTrue { json is JsonPrimitive }
        assertTrue { (json as JsonPrimitive).type == JsonPrimitive.Type.UNKNOWN }
        assertEquals("123.0.0", json.string)
        assertFailsOn(PATH_ROOT) { json.double }
    }

    @Test
    fun emptyArray() {
        val json = adapter.parse("[]")

        assertTrue { json is JsonArray }
        assertEquals(0, json.size)
    }

    @Test
    fun simpleArray() {
        val json = adapter.parse("[1,2,3]")

        assertTrue { json is JsonArray }
        assertEquals(3, json.size)
        assertEquals(listOf(1, 2, 3), json.array.map { it.int })
    }

    @Test
    fun invalidArray() {
        assertFailsWith<JsonParseException> { adapter.parse("[1") }
        assertFailsWith<JsonParseException> { adapter.parse("[1]]") }
    }

    @Test
    fun emptyObject() {
        val json = adapter.parse("{}")

        assertTrue { json is JsonObject }
        assertEquals(0, json.size)
    }

    @Test
    fun simpleObject() {
        val json = adapter.parse("{\"a\":\"b\",\"c\":\"d\"}")

        assertTrue { json is JsonObject }
        assertEquals(2, json.size)
        assertEquals(mapOf("a" to "b", "c" to "d"), json.obj.mapValues { it.value.string })
    }

    @Test
    fun invalidObject() {
        assertFailsWith<JsonParseException> { adapter.parse("{a") }
        assertFailsWith<JsonParseException> { adapter.parse("{a}}") }
        assertFailsWith<JsonParseException> { adapter.parse("{\"a\"}") }
//        assertFailsWith<JsonParseException> { adapter.parse("{'a':\"a\"}") }
//        assertFailsWith<JsonParseException> { adapter.parse("{\"a\":'a'}") }
        assertFailsWith<JsonParseException> { adapter.parse("{\"1\":\"1\"\"2\":\"2\"}") }
    }

    @Test
    fun complexJson() {
        @Language("JSON")
        val json = adapter.parse("""
            {
              "string": "str1",
              "inner": {
                "array": [1, "hello", {"hello": "world"}, [true]]
              }
            }
        """.trimIndent())

        assertTrue { json is JsonObject }
        assertTrue { json["string"] is JsonPrimitive }
        assertTrue { json["inner"] is JsonObject }
        assertTrue { json["inner"]["array"] is JsonArray }
        assertTrue { json["inner"]["array"][0] is JsonPrimitive }
        assertTrue { json["inner"]["array"][1] is JsonPrimitive }
        assertTrue { json["inner"]["array"][2] is JsonObject }
        assertTrue { json["inner"]["array"][3] is JsonArray }

        assertEquals("str1", json["string"].string)
        assertEquals(1, json["inner"]["array"][0].int)
        assertEquals("hello", json["inner"]["array"][1].string)
        assertEquals("world", json["inner"]["array"][2]["hello"].string)
        assertEquals(true, json["inner"]["array"][3][0].bool)
    }
}
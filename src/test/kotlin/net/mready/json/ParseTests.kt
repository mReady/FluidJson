package net.mready.json

import net.mready.json.adapters.JacksonJsonAdapter
import net.mready.json.internal.JsonArrayElement
import net.mready.json.internal.JsonEmptyElement
import net.mready.json.internal.JsonObjectElement
import net.mready.json.internal.JsonPrimitiveElement
import net.mready.json.adapters.KotlinxJsonAdapter
import org.intellij.lang.annotations.Language
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@RunWith(Parameterized::class)
class ParseTests(private val adapter: JsonAdapter) {
    companion object {
        @get:Parameterized.Parameters
        @JvmStatic
        val adapters = listOf(KotlinxJsonAdapter(), JacksonJsonAdapter)
    }

    @Test
    fun emptyString() {
        val json = adapter.parse("")

        assertTrue { json is JsonEmptyElement }
    }

    @Test
    fun plainString() {
        val json = adapter.parse("hello")

        assertTrue { json is JsonPrimitiveElement }
        assertTrue { (json as JsonPrimitiveElement).type == JsonPrimitiveElement.Type.UNKNOWN }
        assertEquals("hello", json.string)
    }

    @Test
    fun quotedString() {
        val json = adapter.parse("\"hello\"")

        assertTrue { json is JsonPrimitiveElement }
        assertTrue { (json as JsonPrimitiveElement).type == JsonPrimitiveElement.Type.UNKNOWN }
        assertEquals("\"hello\"", json.string)
    }

    @Test
    fun plainInt() {
        val json = adapter.parse("123")

        assertTrue { json is JsonPrimitiveElement }
        assertTrue { (json as JsonPrimitiveElement).type == JsonPrimitiveElement.Type.UNKNOWN }
        assertEquals("123", json.string)
        assertEquals(123, json.int)
        assertEquals(123L, json.long)
    }

    @Test
    fun plainDouble() {
        val json = adapter.parse("123.0")

        assertTrue { json is JsonPrimitiveElement }
        assertTrue { (json as JsonPrimitiveElement).type == JsonPrimitiveElement.Type.UNKNOWN }
        assertEquals("123.0", json.string)
        assertEquals(123.0, json.double)
    }

    @Test
    fun invalidDouble() {
        val json = adapter.parse("123.0.0")

        assertTrue { json is JsonPrimitiveElement }
        assertTrue { (json as JsonPrimitiveElement).type == JsonPrimitiveElement.Type.UNKNOWN }
        assertEquals("123.0.0", json.string)
        assertFailsOn(PATH_ROOT) { json.double }
    }

    @Test
    fun emptyArray() {
        val json = adapter.parse("[]")

        assertTrue { json is JsonArrayElement }
        assertEquals(0, json.size)
    }

    @Test
    fun simpleArray() {
        val json = adapter.parse("[1,2,3]")

        assertTrue { json is JsonArrayElement }
        assertEquals(3, json.size)
        assertEquals(listOf(1, 2, 3), json.array.map { it.int })
    }

    @Test
    fun invalidArray() {
        assertFailsWith<JsonParseException> { adapter.parse("[1") }
        if (adapter !is JacksonJsonAdapter) { // TODO: this works with jackson, should it?
            assertFailsWith<JsonParseException> { adapter.parse("[1]]") }
        }
    }

    @Test
    fun emptyObject() {
        val json = adapter.parse("{}")

        assertTrue { json is JsonObjectElement }
        assertEquals(0, json.size)
    }

    @Test
    fun simpleObject() {
        val json = adapter.parse("""{"a":"b","c":"d"}""")

        assertTrue { json is JsonObjectElement }
        assertEquals(2, json.size)
        assertEquals(mapOf<String, String>("a" to "b", "c" to "d"), json.obj.mapValues { it.value.string })
    }

    @Test
    fun invalidObject() {
        assertFailsWith<JsonParseException> { adapter.parse("{a") }
        assertFailsWith<JsonParseException> { adapter.parse("{a}}") }
        assertFailsWith<JsonParseException> { adapter.parse("{\"a\"}") }
//        assertFailsWith<JsonParseException> { adapter.parse("{'a':\"a\"}") }
//        assertFailsWith<JsonParseException> { adapter.parse("{\"a\":'a'}") }
        assertFailsWith<JsonParseException> { adapter.parse("""{"1":"1""2":"2"}""") }
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

        assertTrue { json is JsonObjectElement }
        assertTrue { json["string"] is JsonPrimitiveElement }
        assertTrue { json["inner"] is JsonObjectElement }
        assertTrue { json["inner"]["array"] is JsonArrayElement }
        assertTrue { json["inner"]["array"][0] is JsonPrimitiveElement }
        assertTrue { json["inner"]["array"][1] is JsonPrimitiveElement }
        assertTrue { json["inner"]["array"][2] is JsonObjectElement }
        assertTrue { json["inner"]["array"][3] is JsonArrayElement }

        assertEquals("str1", json["string"].string)
        assertEquals(1, json["inner"]["array"][0].int)
        assertEquals("hello", json["inner"]["array"][1].string)
        assertEquals("world", json["inner"]["array"][2]["hello"].string)
        assertEquals(true, json["inner"]["array"][3][0].bool)
    }
}
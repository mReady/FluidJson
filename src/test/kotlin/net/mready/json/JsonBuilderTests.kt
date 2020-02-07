package net.mready.json

import net.mready.json.kotlinx.KotlinxJsonAdapter
import org.junit.Test
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

class JsonBuilderTests {
    private val adapter: JsonAdapter = KotlinxJsonAdapter()

    @Test
    fun buildSimpleObject() {
        val json = jsonObject {
            "hello" value "world"
        }

        assertEquals("world", json["hello"].string)
        assertEquals("""{"hello":"world"}""", adapter.stringify(json))
    }

    @Test
    fun objectWithPrimitiveValues() {
        val json = jsonObject {
            "string" value "hello"
            "int" value 1
            "long" value 1L
            "double" value 1.0
            "bool" value true
            "null" value null
        }

        assertEquals("hello", json["string"].string)
        assertEquals(1, json["int"].int)
        assertEquals(1L, json["long"].long)
        assertEquals(1.0, json["double"].double)
        assertEquals(true, json["bool"].bool)
        assertEquals(true, json["null"].isNull)

        assertEquals(
            """{"string":"hello","int":1,"long":1,"double":1.0,"bool":true,"null":null}""",
            json.toJsonString(prettyPrint = false)
        )
    }

    @Test
    fun objectWithNestedObject() {
        val json = jsonObject {
            "obj" jsonObject {
                "hello" value "world"
            }
        }

        assertEquals("world", json["obj"]["hello"].string)

        assertEquals("""{"obj":{"hello":"world"}}""", adapter.stringify(json))
    }

    @Test
    fun objectWithNestedArray() {
        val json = jsonObject {
            "arr" jsonArray {
                array += 1
            }
        }

        assertEquals(1, json["arr"][0].int)

        assertEquals("""{"arr":[1]}""", adapter.stringify(json))
    }

    @Test
    fun buildSimpleArray() {
        val json = jsonArray {
            array += 1
            array += 2
            array += 3
        }

        assertEquals(listOf(1, 2, 3), json.array.map { it.int })
        assertEquals("""[1,2,3]""", adapter.stringify(json))
    }

    @Test
    fun arrayWithPrimitiveValues() {
        val json = jsonArray {
            array += "hello"
            array += 1
            array += 1L
            array += 1.0
            array += true
            array += null
        }

        assertEquals("hello", json[0].string)
        assertEquals(1, json[1].int)
        assertEquals(1L, json[2].long)
        assertEquals(1.0, json[3].double)
        assertEquals(true, json[4].bool)
        assertEquals(true, json[5].isNull)

        assertEquals("""["hello",1,1,1.0,true,null]""", adapter.stringify(json))
    }

    @Test
    fun arrayWithNestedObject() {
        val json = jsonArray {
            array += jsonObject {
                "hello" value "world"
            }
        }

        assertEquals("world", json[0]["hello"].string)

        assertEquals("""[{"hello":"world"}]""", adapter.stringify(json))
    }

    @Test
    fun arrayWithNestedArray() {
        val json = jsonArray {
            array += jsonArray {
                array += 1
            }
        }

        assertEquals(1, json[0][0].int)

        assertEquals("""[[1]]""", adapter.stringify(json))
    }
}
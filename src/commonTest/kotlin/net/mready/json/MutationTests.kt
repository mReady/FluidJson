package net.mready.json

import net.mready.json.internal.*
import net.mready.json.adapters.KotlinxJsonAdapter
import kotlin.test.*

open class MutationTests {
    open val adapter: JsonAdapter = KotlinxJsonAdapter()

    @Test
    fun objectFromEmpty() {
        val json = FluidJson()
        json["null"] = null
        json["string"] = "string"
        json["int"] = 1
        json["long"] = 1L
        json["double"] = 1.0
        json["bool"] = true
        json["array"] = listOf(1, 2, 3)
        json["obj"] = jsonObject {
            obj["a"] = 1
        }

        assertTrue { json["null"] is JsonNullElement }
        assertEquals(null, json["null"].orNull)

        assertTrue { json["string"] is JsonPrimitiveElement }
        assertEquals("string", json["string"].string)

        assertTrue { json["int"] is JsonPrimitiveElement }
        assertEquals(1, json["int"].int)

        assertTrue { json["long"] is JsonPrimitiveElement }
        assertEquals(1L, json["long"].long)

        assertTrue { json["bool"] is JsonPrimitiveElement }
        assertEquals(true, json["bool"].bool)

        assertTrue { json["array"] is JsonArrayElement }
        assertEquals(listOf(1, 2, 3), json["array"].array.map { it.int })

        assertTrue { json["obj"] is JsonObjectElement }
        assertEquals(mapOf<String, Int>("a" to 1), json["obj"].obj.mapValues { it.value.int })

        assertEquals("""{"null":null,"string":"string","int":1,"long":1,"double":1.0,"bool":true,"array":[1,2,3],"obj":{"a":1}}""", adapter.stringify(json))
    }

    @Test
    fun materializeAsObject() {
        val json = jsonObject { }
        json["obj"]["inner1"]["inner2"]["value"] = 1

        assertNotNull(json["obj"].objOrNull)
        assetSucceeds { json["obj"].obj }
        assertNotNull(json["obj"]["inner1"].objOrNull)
        assetSucceeds { json["obj"]["inner1"].obj }
        assertEquals(1, json["obj"]["inner1"]["inner2"]["value"].int)

        assertNull(json["obj"].arrayOrNull)
        assertNull(json["obj"]["inner1"].arrayOrNull)
        assertFailsOn("$['obj']") { json["obj"].array }
        assertFailsOn("$['obj']['inner1']") { json["obj"]["inner1"].array }

        assertEquals("""{"obj":{"inner1":{"inner2":{"value":1}}}}""", adapter.stringify(json))
    }

    @Test
    fun materializeAsArray() {
        val json = jsonObject { }
        json["arr"][0][0] = 1

        assertNotNull(json["arr"].arrayOrNull)
        assetSucceeds { json["arr"].array }
        assertNotNull(json["arr"][0].arrayOrNull)
        assetSucceeds { json["arr"][0].array }
        assertEquals(1, json["arr"][0][0].int)

        assertNull(json["arr"].objOrNull)
        assertNull(json["arr"][0].objOrNull)
        assertFailsOn("$['arr']") { json["arr"].obj }
        assertFailsOn("$['arr'][0]") { json["arr"][0].obj }

        assertEquals("""{"arr":[[1]]}""", adapter.stringify(json))
    }

    @Test
    fun fillInArrayValues() {
        val json = FluidJson()
        json[5] = 5
        json.array.take(5).forEach {
            assertNull(it.orNull)
            assertTrue { it is JsonEmptyElement }
        }

        json[2] = 2
        assertEquals(2, json[2].int)
        assertEquals(5, json[5].int)

        assertEquals("""[null,null,2,null,null,5]""", adapter.stringify(json))
    }

    @Test
    fun failOnExplicitNull() {
        val json = jsonObject {
            obj["null"] = null
        }

        assertFailsOn("$['null']") { json["null"]["value"] = 1 }
        assertFailsOn("$['null']") { json["null"][0] = 1 }
    }

    @Test
    fun updatePathOnCopy() {
        val json = jsonObject {  }
        val inner = jsonObject {
            obj["value"] = 1
        }

        json["obj"]["inner"] = inner
        json["arr"][0] = inner

        assertFailsOn("$['obj']['inner']['value']") { json["obj"]["inner"]["value"].bool }
        assertFailsOn("$['arr'][0]['value']") { json["arr"][0]["value"].bool }

        assertEquals("""{"obj":{"inner":{"value":1}},"arr":[{"value":1}]}""", adapter.stringify(json))
    }

    @Test
    fun jsonSetterOperators() {
        val json = FluidJson()
        json["null"] = null
        json["hello"] = 123
        json["obj"]["sub"] = "1234"
        json["arr"][1] = true
        json["arr2"] += 1
        json["arr2"] += 2
        json["arr2"][2][0] = 3

        assertEquals(true, json["null"].isNull)
        assertEquals(123, json["hello"].int)
        assertEquals("1234", json["obj"]["sub"].string)
        assertEquals(true, json["arr"][0].isNull)
        assertEquals(true, json["arr"][1].bool)
        assertEquals(1, json["arr2"][0].int)
        assertEquals(2, json["arr2"][1].int)
        assertEquals(3, json["arr2"][2][0].int)

        assertEquals(
            """{"null":null,"hello":123,"obj":{"sub":"1234"},"arr":[null,true],"arr2":[1,2,[3]]}""",
            adapter.stringify(json)
        )
    }

    @Test
    fun deleteObjectKey() {
        val json = jsonObject {
            obj["hello"] = "world"
        }
        assertEquals("world", json["hello"].string)

        json.delete("hello")
        assertTrue { json["hello"].isNull }
    }

    @Test
    fun deleteArrayIndex() {
        val json = jsonArray {
            array[0] = 1
            array[1] = 2
            array[2] = 3
        }

        assertEquals(listOf(1, 2, 3), json.array.map { it.int })
        json.delete(1)
        assertEquals(listOf(1, 3), json.array.map { it.int })
    }
}
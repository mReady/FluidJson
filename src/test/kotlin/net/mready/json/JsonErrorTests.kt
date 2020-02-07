package net.mready.json

import net.mready.json.kotlinx.KotlinxJsonAdapter
import org.junit.Test
import kotlin.test.assertEquals

class JsonErrorTests {
    private val adapter: JsonAdapter = KotlinxJsonAdapter()

    @Test
    fun primitiveAsStructure() {
        val json = jsonObject {
            "int" value 1
        }

        assertEquals(1, json["int"].int)

        assertFailsOn(PATH_ROOT, "int") { json["int"][0].int }
        assertFailsOn(PATH_ROOT, "int") { json["int"][0] = 1 }
        assertEquals(true, json["int"][0].isNull)
        assertEquals(null, json["int"][0].orNull)

        assertFailsOn(PATH_ROOT, "int") { json["int"]["a"].int }
        assertFailsOn(PATH_ROOT, "int") { json["int"]["a"] = 1 }
        assertEquals(true, json["int"]["a"].isNull)
        assertEquals(null, json["int"]["a"].orNull)
    }

    @Test
    fun structureAsPrimitive() {
        val json = jsonObject {
            "obj" value jsonObject { }
            "arr" value jsonArray { }
        }

        assertFailsOn(PATH_ROOT, "obj") { json["obj"].string }
        assertFailsOn(PATH_ROOT, "arr") { json["arr"].string }
    }

    @Test
    fun invalidStructureType() {
        val json = jsonObject {
            "obj" value jsonObject { }
            "arr" value jsonArray { }
        }

        assertFailsOn(PATH_ROOT, "obj") { json["obj"].array }
        assertFailsOn(PATH_ROOT, "obj") { json["obj"][0].string }
        assertFailsOn(PATH_ROOT, "arr") { json["arr"].obj }
        assertFailsOn(PATH_ROOT, "arr") { json["arr"]["a"].string }
    }

    @Test
    fun nullAsPrimitive() {
        val json = jsonObject {
            "null" value null
        }

        assertEquals(true, json["null"].isNull)
        assertEquals(null, json["null"].orNull)
        assertFailsOn(PATH_ROOT, "null") { json["null"].size }
        assertFailsOn(PATH_ROOT, "null") { json["null"].string }
        assertFailsOn(PATH_ROOT, "null") { json["null"].int }
        assertFailsOn(PATH_ROOT, "null") { json["null"].long }
        assertFailsOn(PATH_ROOT, "null") { json["null"].double }
    }

    @Test
    fun nullAsStructure() {
        val json = jsonObject {
            "null" value null
        }

        assertEquals(true, json["null"][0].isNull)
        assertEquals(null, json["null"][0].orNull)

        assertEquals(true, json["null"]["a"].isNull)
        assertEquals(null, json["null"]["a"].orNull)

        assertFailsOn(PATH_ROOT, "null") { json["null"][0].string }
        assertFailsOn(PATH_ROOT, "null") { json["null"][0][1][2].string }
        assertFailsOn(PATH_ROOT, "null") { json["null"]["a"].string }
        assertFailsOn(PATH_ROOT, "null") { json["null"]["a"]["b"]["c"].string }
    }

    @Test
    fun outOfBounds() {
        val json = jsonObject {
            "arr" jsonArray {}
            "obj" jsonObject {}
        }

        assertFailsOn(PATH_ROOT, "arr") { json["arr"][10].string }
        assertFailsOn(PATH_ROOT, "arr") { json["arr"][-1] }
        assertFailsOn(PATH_ROOT, "arr") { json["arr"][-1] = 1 }
        assertFailsOn(PATH_ROOT, "obj") { json["obj"]["a"].string }
    }

    @Test
    fun complexStructurePath() {
        val json = jsonObject {
            "inner1" jsonObject {
                "arr1" jsonArray {
                    array += jsonObject {
                        "inner2" jsonObject {
                            obj["arr2"] = jsonArray {}
                        }
                    }
                }
            }
        }

        assertFailsOn(PATH_ROOT, "inner1") {
            json["inner1"]["invalid"].string
        }
        assertFailsOn(PATH_ROOT, "inner1", "arr1") {
            json["inner1"]["arr1"][1].string
        }
        assertFailsOn(PATH_ROOT, "inner1", "arr1", "[0]") {
            json["inner1"]["arr1"][0]["invalid"].string
        }
        assertFailsOn(PATH_ROOT, "inner1", "arr1", "[0]", "inner2") {
            json["inner1"]["arr1"][0]["inner2"][0].string
        }
        assertFailsOn(PATH_ROOT, "inner1", "arr1", "[0]", "inner2", "arr2") {
            json["inner1"]["arr1"][0]["inner2"]["arr2"][0].string
        }
    }
}
package net.mready.json

import net.mready.json.adapters.KotlinxJsonAdapter
import kotlin.test.Test
import kotlin.test.assertEquals

open class JsonErrorTests {
    open val adapter: JsonAdapter = KotlinxJsonAdapter()

    @Test
    fun primitiveAsStructure() {
        val json = jsonObject {
            obj["int"] = 1
        }

        assertEquals(1, json["int"].int)

        assertFailsOn("$['int']") { json["int"][0].int }
        assertFailsOn("$['int']") { json["int"][0] = 1 }
        assertEquals(true, json["int"][0].isNull)
        assertEquals(null, json["int"][0].orNull)

        assertFailsOn("$['int']") { json["int"]["a"].int }
        assertFailsOn("$['int']") { json["int"]["a"] = 1 }
        assertEquals(true, json["int"]["a"].isNull)
        assertEquals(null, json["int"]["a"].orNull)
    }

    @Test
    fun structureAsPrimitive() {
        val json = jsonObject {
            obj["obj"] = jsonObject { }
            obj["arr"] = jsonArray { }
        }

        assertFailsOn("$['obj']") { json["obj"].string }
        assertFailsOn("$['arr']") { json["arr"].string }
    }

    @Test
    fun invalidStructureType() {
        val json = jsonObject {
            obj["obj"] = jsonObject { }
            obj["arr"] = jsonArray { }
        }

        assertFailsOn("$['obj']") { json["obj"].array }
        assertFailsOn("$['obj']") { json["obj"][0].string }
        assertFailsOn("$['arr']") { json["arr"].obj }
        assertFailsOn("$['arr']") { json["arr"]["a"].string }
    }

    @Test
    fun nullAsPrimitive() {
        val json = jsonObject {
            obj["null"] = null
        }

        assertEquals(true, json["null"].isNull)
        assertEquals(null, json["null"].orNull)
        assertFailsOn("$['null']") { json["null"].size }
        assertFailsOn("$['null']") { json["null"].string }
        assertFailsOn("$['null']") { json["null"].int }
        assertFailsOn("$['null']") { json["null"].long }
        assertFailsOn("$['null']") { json["null"].double }
    }

    @Test
    fun nullAsStructure() {
        val json = jsonObject {
            obj["null"] = null
        }

        assertEquals(true, json["null"][0].isNull)
        assertEquals(null, json["null"][0].orNull)

        assertEquals(true, json["null"]["a"].isNull)
        assertEquals(null, json["null"]["a"].orNull)

        assertFailsOn("$['null']") { json["null"][0].string }
        assertFailsOn("$['null']") { json["null"][0][1][2].string }
        assertFailsOn("$['null']") { json["null"]["a"].string }
        assertFailsOn("$['null']") { json["null"]["a"]["b"]["c"].string }
    }

    @Test
    fun outOfBounds() {
        val json = jsonObject {
            obj["arr"] = jsonArray {}
            obj["obj"] = jsonObject {}
        }

        assertFailsOn("$['arr']") { json["arr"][10].string }
        assertFailsOn("$['arr']") { json["arr"][-1] }
        assertFailsOn("$['arr']") { json["arr"][-1] = 1 }
        assertFailsOn("$['obj']") { json["obj"]["a"].string }
    }

    @Test
    fun complexStructurePath() {
        val json = jsonObject {
            obj["inner1"] = jsonObject {
                obj["arr1"] = jsonArray {
                    array += jsonObject {
                        obj["inner2"] = jsonObject {
                            obj["arr2"] = jsonArray {}
                        }
                    }
                }
            }
        }

        assertFailsOn("$['inner1']") {
            json["inner1"]["invalid"].string
        }
        assertFailsOn("$['inner1']['arr1']") {
            json["inner1"]["arr1"][1].string
        }
        assertFailsOn("$['inner1']['arr1'][0]") {
            json["inner1"]["arr1"][0]["invalid"].string
        }
        assertFailsOn("$['inner1']['arr1'][0]['inner2']") {
            json["inner1"]["arr1"][0]["inner2"][0].string
        }
        assertFailsOn("$['inner1']['arr1'][0]['inner2']['arr2']") {
            json["inner1"]["arr1"][0]["inner2"]["arr2"][0].string
        }
    }
}
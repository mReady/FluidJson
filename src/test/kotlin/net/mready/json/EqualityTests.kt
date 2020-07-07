package net.mready.json

import net.mready.json.internal.JsonPrimitiveElement
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class EqualityTests {
    @Test
    fun primitiveEquals() {
        assertEquals(Json("123"), Json("123"))
        assertNotEquals(Json("123"), Json("1234"))

        assertEquals(Json(123), Json(123))
        assertNotEquals(Json(123), Json(1234))

        assertEquals(Json(123L), Json(123L))
        assertNotEquals(Json(123L), Json(1234L))

        assertEquals(Json(123.0), Json(123.0))
        assertNotEquals(Json(123.0), Json(123.4))

        assertEquals(Json(123), Json(123L))
        assertEquals(Json(123), Json(123.0))

        assertEquals(Json(true), Json(true))
        assertNotEquals(Json(true), Json(false))

        assertNotEquals(Json(123), Json("123"))

        assertEquals(
            JsonPrimitiveElement("123", JsonPrimitiveElement.Type.UNKNOWN, adapter = defaultJsonAdapter),
            JsonPrimitiveElement("123", JsonPrimitiveElement.Type.UNKNOWN, adapter = defaultJsonAdapter)
        )

        assertEquals(
            JsonPrimitiveElement("123", JsonPrimitiveElement.Type.NUMBER, adapter = defaultJsonAdapter),
            JsonPrimitiveElement("123", JsonPrimitiveElement.Type.UNKNOWN, adapter = defaultJsonAdapter)
        )

        assertEquals(
            JsonPrimitiveElement("123", JsonPrimitiveElement.Type.STRING, adapter = defaultJsonAdapter),
            JsonPrimitiveElement("123", JsonPrimitiveElement.Type.UNKNOWN, adapter = defaultJsonAdapter)
        )
    }

    @Test
    fun nullEqual() {
        assertEquals(Json(null), Json(null))
    }

    @Test
    fun objectEquals() {
        assertEquals(jsonObject {
            "a" value 1
        }, jsonObject {
            "a" value 1
        })

        assertNotEquals(jsonObject {
            "a" value 1
            "b" value 2
        }, jsonObject {
            "a" value 1
        })

        assertEquals(Json().apply {
            this["a"] = 1
        }, jsonObject {
            obj["a"] = 1
        })
    }

    @Test
    fun arrayEquals() {
        assertEquals(jsonArray {
            array += 1
        }, jsonArray {
            array += 1
        })

        assertNotEquals(jsonArray {
            array += 1
            array += 2
        }, jsonArray {
            array += 1
        })

        assertEquals(Json().apply {
            this[0] = 1
        }, jsonArray {
            array[0] = 1
        })
    }

    @Test
    fun emptyEquals() {
        assertEquals(Json(), Json())

        assertEquals(Json().apply {
            this["a"] = 1
        }, Json().apply {
            this["a"] = 1
        })

        assertNotEquals(Json().apply {
            this["a"] = 1
        }, Json().apply {
            this["b"] = 1
        })

        assertEquals(jsonObject {
            obj["a"] = 1
        }, Json().apply {
            this["a"] = 1
        })

        assertEquals(jsonArray {
            array[0] = 1
        }, Json().apply {
            this[0] = 1
        })
    }
}
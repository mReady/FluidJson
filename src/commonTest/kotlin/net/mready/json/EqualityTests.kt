package net.mready.json

import net.mready.json.internal.JsonPrimitiveElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

open class EqualityTests {
    @Test
    fun primitiveEquals() {
        assertEquals(Json.wrap("123"), Json.wrap("123"))
        assertNotEquals(Json.wrap("123"), Json.wrap("1234"))

        assertEquals(Json.wrap(123), Json.wrap(123))
        assertNotEquals(Json.wrap(123), Json.wrap(1234))

        assertEquals(Json.wrap(123L), Json.wrap(123L))
        assertNotEquals(Json.wrap(123L), Json.wrap(1234L))

        assertEquals(Json.wrap(123.0), Json.wrap(123.0))
        assertNotEquals(Json.wrap(123.0), Json.wrap(123.4))

        assertEquals(Json.wrap(123), Json.wrap(123L))
        assertEquals(Json.wrap(123), Json.wrap(123.0))

        assertEquals(Json.wrap(true), Json.wrap(true))
        assertNotEquals(Json.wrap(true), Json.wrap(false))

        assertNotEquals(Json.wrap(123), Json.wrap("123"))

        assertEquals(
            JsonPrimitiveElement("123", JsonPrimitiveElement.Type.UNKNOWN, adapter = FluidJson),
            JsonPrimitiveElement("123", JsonPrimitiveElement.Type.UNKNOWN, adapter = FluidJson)
        )

        assertEquals(
            JsonPrimitiveElement("123", JsonPrimitiveElement.Type.NUMBER, adapter = FluidJson),
            JsonPrimitiveElement("123", JsonPrimitiveElement.Type.UNKNOWN, adapter = FluidJson)
        )

        assertEquals(
            JsonPrimitiveElement("123", JsonPrimitiveElement.Type.STRING, adapter = FluidJson),
            JsonPrimitiveElement("123", JsonPrimitiveElement.Type.UNKNOWN, adapter = FluidJson)
        )
    }

    @Test
    fun nullEqual() {
        assertEquals(Json.wrap(null), Json.wrap(null))
    }

    @Test
    fun objectEquals() {
        assertEquals(jsonObject {
            obj["a"] = 1
        }, jsonObject {
            obj["a"] = 1
        })

        assertNotEquals(jsonObject {
            obj["a"] = 1
            obj["b"] = 2
        }, jsonObject {
            obj["a"] = 1
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
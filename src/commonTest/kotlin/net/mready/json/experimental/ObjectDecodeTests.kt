package net.mready.json.experimental

import kotlinx.serialization.Serializable
import net.mready.json.JsonAdapter
import net.mready.json.adapters.KotlinxJsonAdapter
import net.mready.json.decode
import net.mready.json.jsonArray
import net.mready.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotEquals

open class ObjectDecodeTests {
    open val adapter: JsonAdapter = KotlinxJsonAdapter()

    @Serializable
    data class SimpleObject(val value: Int)

    @Serializable
    data class SimpleObjectAlt(val value: Int)

    @Serializable
    data class SimpleObject2(val value: Int, val value2: Int)

    @Serializable
    data class TypedObject<T>(val inner: T)

    @Test
    fun decodePrimitive() {
        assertEquals(1, adapter.wrap(1).decode())
        assertEquals(1.0, adapter.wrap(1.0).decode())
        assertEquals("test", adapter.wrap("test").decode())
        assertEquals(true, adapter.wrap(true).decode())
        assertEquals<String?>(null, adapter.wrap(null).decode())
    }

    @Test
    fun decodeStructuredElement() {
        assertEquals(listOf(1), jsonArray(adapter) { array += 1 }.decode())
        assertEquals(mapOf("test" to 1), jsonObject(adapter) { obj["test"] = 1 }.decode())
        assertEquals(mapOf("test" to 1), adapter.newJson().apply { this["test"] = 1 }.decode())
    }

    @Test
    fun decodeObjectIntoType() {
        assertEquals(
            SimpleObject(1),
            jsonObject(adapter) { obj["value"] = 1 }.decode()
        )
        assertEquals(
            SimpleObject(1),
            adapter.newJson().apply { this["value"] = 1 }.decode()
        )
        assertEquals(
            TypedObject(SimpleObject(1)),
            jsonObject(adapter) {
                obj["inner"] = jsonObject {
                    obj["value"] = 1
                }
            }.decode()
        )

        assertFails {
            jsonObject(adapter) { obj["value"] = 1 }.decode<Any>()
        }
    }

    @Test
    fun decodeRef() {
        assertEquals(
            listOf(1),
            adapter.wrap(listOf(1)).decode()
        )
        assertEquals(
            mapOf("test" to 1),
            adapter.wrap(mapOf("test" to 1)).decode()
        )
        assertEquals(
            SimpleObject(1),
            adapter.wrap(SimpleObject(1)).decode()
        )
        assertEquals(
            TypedObject(SimpleObject(1)),
            adapter.wrap(TypedObject(SimpleObject(1))).decode()
        )
    }

    @Test
    fun decodeUnwrappedRef() {
        assertEquals(
            listOf(1, 2),
            adapter.wrap(listOf(1)).apply {
                this[1] = 2
            }.decode()
        )
        assertEquals(
            mapOf("test1" to 1, "test2" to 2),
            adapter.wrap(mapOf("test1" to 1)).apply {
                this["test2"] = 2
            }.decode()
        )

        assertEquals(
            SimpleObject2(1, 2),
            adapter.wrap(SimpleObject(1)).apply {
                this["value2"] = 2
            }.decode()
        )

        assertEquals(
            TypedObject(SimpleObject2(1, 2)),
            adapter.wrap(TypedObject(SimpleObject(1))).apply {
                this["inner"]["value2"] = 2
            }.decode()
        )
    }

    @Test
    fun decodeRefIntoAnotherType() {
        assertEquals(
            SimpleObject(1),
            adapter.wrap(SimpleObjectAlt(1)).decode()
        )
        assertEquals(
            SimpleObject(1),
            adapter.wrap(SimpleObject2(1, 2)).decode()
        )
        assertEquals(
            TypedObject(SimpleObject(1)),
            adapter.wrap(TypedObject(SimpleObjectAlt(1))).decode()
        )
    }

    @Test
    fun decodeErasedRef() {
        assertEquals(
            SimpleObject(1),
            adapter.wrap<Any?>(SimpleObject(1)).decode()
        )
        assertEquals(
            SimpleObjectAlt(1),
            adapter.wrap<Any?>(SimpleObject(1)).decode()
        )

        assertFails {
            assertEquals(
                TypedObject(SimpleObject(1)),
                adapter.wrap<Any?>(TypedObject(SimpleObject(1))).decode()
            )
        }

        assertFails {
            assertEquals(
                TypedObject(SimpleObjectAlt(1)),
                adapter.wrap<Any?>(TypedObject(SimpleObject(1))).decode()
            )
        }
    }

    @Test
    fun decodeRefWithErasedDecode() {
        assertEquals(
            SimpleObject(1),
            adapter.wrap(SimpleObject(1)).decode<Any?>()
        )
        assertEquals(
            TypedObject(SimpleObject(1)),
            adapter.wrap(TypedObject(SimpleObject(1))).decode<Any?>()
        )

        assertNotEquals(
            SimpleObjectAlt(1),
            adapter.wrap(SimpleObject(1)).decode<Any?>()
        )
        assertNotEquals(
            TypedObject(SimpleObjectAlt(1)),
            adapter.wrap(TypedObject(SimpleObject(1))).decode<Any?>()
        )
    }

    @Test
    fun decodeRefWithAllErased() {
        assertEquals(
            SimpleObject(1),
            adapter.wrap<Any?>(SimpleObject(1)).decode<Any?>()
        )
        assertEquals(
            TypedObject(SimpleObject(1)),
            adapter.wrap<Any?>(TypedObject(SimpleObject(1))).decode<Any?>()
        )

        assertNotEquals(
            SimpleObjectAlt(1),
            adapter.wrap<Any?>(SimpleObject(1)).decode<Any?>()
        )
        assertNotEquals(
            TypedObject(SimpleObjectAlt(1)),
            adapter.wrap<Any?>(TypedObject(SimpleObject(1))).decode<Any?>()
        )
    }
}
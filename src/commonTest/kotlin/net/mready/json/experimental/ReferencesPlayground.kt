package net.mready.json.experimental

import kotlinx.serialization.Serializable
import net.mready.json.FluidJson
import net.mready.json.JsonAdapter
import net.mready.json.adapters.KotlinxJsonAdapter
import net.mready.json.decode
import net.mready.json.jsonObject
import kotlin.test.Test

@Serializable
data class A(val a: Int = 1, val b: Int = 2)

@Serializable
data class B(val a: A = A())

@Serializable
data class C(val t: B)

@Serializable
data class T1(
    val a: Int
)

@Serializable
data class T2(
    val a: Int,
    val b: Int
)

@Serializable
data class W<T>(val v: T)

open class ReferenceTests {
    open val adapter: JsonAdapter = KotlinxJsonAdapter()

    @Test
    fun test() {

        println(FluidJson.wrap(1).decode<Int>())

        val json: FluidJson = adapter.wrap(C(B()))
        println(json.decode<C>())
        println(json.toJsonString())

        val json2 = adapter.parse(json.toJsonString())
        println(json2.decode<C>())
        json2["t"] = null
        json2["a"] = 1
        println(json2.decode<T1>())

        val json3 = adapter.wrap(T1(1))
        println(json3["a"].int)
        json3["b"] = 2
        println(json3.decode<T2>())

        val json5 = adapter.parse("""{"v": "asd"}""")
        println(json5.decode<W<String>>())

        val json4 = adapter.wrap(W(1))
        println(json4.toJsonString())

        println(adapter.encodeObject(listOf(1, 2, 3)))
        println(adapter.decodeObject<List<Int>>("""[1, 2, 3]""").toString())

    }

    @Test
    fun test2() {
        fun x(value: Any): FluidJson {
            return adapter.wrap(value)
        }

        println(x(A()).toJsonString())
    }
}
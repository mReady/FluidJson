package net.mready.json.experimental

import kotlinx.serialization.*
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import net.mready.json.*
import net.mready.json.adapters.JacksonJsonAdapter
import net.mready.json.adapters.KotlinxJsonAdapter
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

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

@RunWith(Parameterized::class)
@OptIn(ExperimentalUserTypes::class)
class ReferenceTests(private val adapter: JsonAdapter) {
    companion object {
        @get:Parameterized.Parameters
        @JvmStatic
        val adapters = listOf(KotlinxJsonAdapter(), JacksonJsonAdapter)
    }

    @Test
    fun test() {
        val json: FluidJson = FluidJson.ref(C(B()), adapter = adapter)
        println(json.value<C>())
        //println(Json.stringify(json))
        println(json.toJsonString())

        val json2 = adapter.parse(json.toJsonString())
        println(json2.value<C>())
        json2["t"] = null
        json2["a"] = 1
        println(json2.value<T1>())

        val json3 = FluidJson.ref(T1(1), adapter = adapter)
        println(json3["a"].int)
        json3["b"] = 2
        println(json3.value<T2>())

        val json5 = adapter.parse("""{"v": "asd"}""")
        println(json5.value<W<String>>())

        val json4 = FluidJson.ref(W(1), adapter = adapter)
        println(json4.toJsonString())


    }
}
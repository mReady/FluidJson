@file:Suppress("UNUSED_PARAMETER", "unused")

package net.mready.json

import net.mready.json.internal.JsonArray
import net.mready.json.internal.JsonObject


@DslMarker
annotation class JsonDslMarker

inline fun jsonObject(block: JsonObjectDsl.() -> Unit): FluidJson {
    return JsonObjectDsl().apply(block).build()
}

inline fun jsonArray(block: JsonArrayDsl.() -> Unit): FluidJson {
    return JsonArrayDsl().apply(block).build()
}

@JsonDslMarker
open class JsonDsl(@PublishedApi internal val path: JsonPath = JsonPath.ROOT) {
    inline fun jsonArray(block: JsonArrayDsl.() -> Unit): FluidJson {
        return JsonArrayDsl(path).apply(block).build()
    }

    inline fun jsonObject(block: JsonObjectDsl.() -> Unit): FluidJson {
        return JsonObjectDsl(path).apply(block).build()
    }
}

@JsonDslMarker
class JsonObjectDsl(path: JsonPath = JsonPath.ROOT) : JsonDsl(path) {
    val obj: FluidJson = JsonObject(mutableMapOf(), path)

    infix fun String.value(value: Nothing?) {
        obj[this] = null
    }

    infix fun String.value(value: String?) {
        obj[this] = value
    }

    infix fun String.value(value: Number?) {
        obj[this] = value
    }

    infix fun String.value(value: Boolean?) {
        obj[this] = value
    }

    infix fun String.value(value: FluidJson?) {
        obj[this] = value
    }

    inline infix fun String.jsonArray(block: JsonArrayDsl.() -> Unit) {
        obj[this] = JsonArrayDsl(path + this).apply(block).build()
    }

    inline infix fun String.jsonObject(block: JsonObjectDsl.() -> Unit) {
        obj[this] = JsonObjectDsl(path + this).apply(block).build()
    }

    @PublishedApi
    internal fun build(): FluidJson {
        return obj
    }
}

@JsonDslMarker
class JsonArrayDsl(path: JsonPath = JsonPath.ROOT) : JsonDsl(path) {
    val array: FluidJson = JsonArray(mutableListOf(), path)

    fun emit(value: Nothing?) {
        array += null
    }

    fun emit(value: String?) {
        array += value
    }

    fun emit(value: Number?) {
        array += value
    }

    fun emit(value: Boolean?) {
        array += value
    }

    fun emit(value: FluidJson?) {
        array += value
    }

    inline fun <T> Collection<T>.emitEach(block: (T) -> FluidJson) {
        forEach { emit(block(it)) }
    }

    @PublishedApi
    internal fun build(): FluidJson {
        return array
    }
}
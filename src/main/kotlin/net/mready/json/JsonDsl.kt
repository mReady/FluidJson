@file:Suppress("UNUSED_PARAMETER", "unused")

package net.mready.json


@DslMarker
annotation class JsonDslMarker

inline fun jsonObject(block: JsonObjectDsl.() -> Unit): JsonValue {
    return JsonObjectDsl().apply(block).build()
}

inline fun jsonArray(block: JsonArrayDsl.() -> Unit): JsonValue {
    return JsonArrayDsl().apply(block).build()
}

@JsonDslMarker
open class JsonDsl(@PublishedApi internal val path: JsonPath = JsonPath.ROOT) {
    inline fun jsonArray(block: JsonArrayDsl.() -> Unit): JsonValue {
        return JsonArrayDsl(path).apply(block).build()
    }

    inline fun jsonObject(block: JsonObjectDsl.() -> Unit): JsonValue {
        return JsonObjectDsl(path).apply(block).build()
    }
}

@JsonDslMarker
class JsonObjectDsl(path: JsonPath = JsonPath.ROOT) : JsonDsl(path) {
    val obj: JsonValue = JsonObject(mutableMapOf(), path)

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

    infix fun String.value(value: JsonValue?) {
        obj[this] = value
    }

    inline infix fun String.jsonArray(block: JsonArrayDsl.() -> Unit) {
        obj[this] = JsonArrayDsl(path + this).apply(block).build()
    }

    inline infix fun String.jsonObject(block: JsonObjectDsl.() -> Unit) {
        obj[this] = JsonObjectDsl(path + this).apply(block).build()
    }

    @PublishedApi
    internal fun build(): JsonValue {
        return obj
    }
}

@JsonDslMarker
class JsonArrayDsl(path: JsonPath = JsonPath.ROOT) : JsonDsl(path) {
    val array: JsonValue = JsonArray(mutableListOf(), path)

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

    fun emit(value: JsonValue?) {
        array += value
    }

    inline fun <T> Collection<T>.emitEach(block: (T) -> JsonValue) {
        forEach { emit(block(it)) }
    }

    @PublishedApi
    internal fun build(): JsonValue {
        return array
    }
}
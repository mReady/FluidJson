@file:Suppress("UNUSED_PARAMETER", "unused")

package net.mready.json

import net.mready.json.internal.JsonArrayElement
import net.mready.json.internal.JsonObjectElement
import net.mready.json.internal.JsonPath


@DslMarker
annotation class JsonDslMarker

inline fun jsonObject(adapter: JsonAdapter = FluidJson, block: JsonObjectDsl.() -> Unit): FluidJson {
    return JsonObjectDsl(adapter = adapter).apply(block).build()
}

inline fun jsonArray(adapter: JsonAdapter = FluidJson, block: JsonArrayDsl.() -> Unit): FluidJson {
    return JsonArrayDsl(adapter = adapter).apply(block).build()
}

@JsonDslMarker
open class JsonDsl(
    @PublishedApi internal val path: JsonPath,
    @PublishedApi internal val adapter: JsonAdapter
) {
    inline fun jsonArray(block: JsonArrayDsl.() -> Unit): FluidJson {
        return JsonArrayDsl(path, adapter).apply(block).build()
    }

    inline fun jsonObject(block: JsonObjectDsl.() -> Unit): FluidJson {
        return JsonObjectDsl(path, adapter).apply(block).build()
    }
}

@JsonDslMarker
class JsonObjectDsl(
    path: JsonPath = JsonPath.ROOT,
    adapter: JsonAdapter = FluidJson
) : JsonDsl(path, adapter) {
    val obj: FluidJson = JsonObjectElement(mutableMapOf(), path, adapter)

    @PublishedApi
    internal fun build(): FluidJson {
        return obj
    }
}

@JsonDslMarker
class JsonArrayDsl(
    path: JsonPath = JsonPath.ROOT,
    adapter: JsonAdapter = FluidJson
) : JsonDsl(path, adapter) {
    val array: FluidJson = JsonArrayElement(mutableListOf(), path, adapter)

    @PublishedApi
    internal fun build(): FluidJson {
        return array
    }
}
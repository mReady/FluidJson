package net.mready.json.internal

import net.mready.json.FluidJson
import net.mready.json.JsonAdapter

internal inline fun <T> jsonNullOr(
    value: T?,
    path: JsonPath,
    adapter: JsonAdapter,
    block: (T) -> FluidJson
): FluidJson {
    return if (value == null) {
        JsonNull(path, adapter)
    } else {
        block(value)
    }
}

internal fun <T> wrapValue(value: T, path: JsonPath = JsonPath.ROOT, adapter: JsonAdapter): FluidJson {
    return when (value) {
        null -> JsonNull(path, adapter)
        is String -> JsonPrimitive(value, JsonPrimitive.Type.STRING, path, adapter)
        is Number -> JsonPrimitive(value.toString(), JsonPrimitive.Type.NUMBER, path, adapter)
        is Boolean -> JsonPrimitive(value.toString(), JsonPrimitive.Type.BOOLEAN, path, adapter)
        is JsonElement -> value.copy(path, adapter)
        is Collection<*> -> JsonArray(
            content = value.mapIndexedTo(mutableListOf()) { index, item ->
                wrapValue(item, path + index, adapter)
            },
            path = path,
            adapter = adapter
        )
        is Map<*, *> -> JsonObject(
            content = value.entries.fold(mutableMapOf()) { acc, v ->
                acc[v.key.toString()] =
                    wrapValue(v.value, path + v.key.toString(), adapter);
                return@fold acc
            },
            path = path,
            adapter = adapter
        )
        else -> throw AssertionError()
    }
}
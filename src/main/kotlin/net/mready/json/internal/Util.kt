package net.mready.json.internal

import net.mready.json.FluidJson
import net.mready.json.JsonPath

internal inline fun <T> jsonNullOr(value: T?, path: JsonPath, block: (T) -> FluidJson): FluidJson {
    return if (value == null) {
        JsonNull(path)
    } else {
        block(value)
    }
}

internal fun <T> wrapValue(value: T, path: JsonPath = JsonPath.ROOT): FluidJson {
    return when (value) {
        null -> JsonNull(path)
        is String -> JsonPrimitive(value, JsonPrimitive.Type.STRING, path)
        is Number -> JsonPrimitive(value.toString(), JsonPrimitive.Type.NUMBER, path)
        is Boolean -> JsonPrimitive(value.toString(), JsonPrimitive.Type.BOOLEAN, path)
        is JsonElement -> value.copyWithPath(path)
        is Collection<*> -> JsonArray(
            content = value.mapIndexedTo(mutableListOf()) { index, item ->
                wrapValue(item, path + index)
            },
            path = path
        )
        is Map<*, *> -> JsonObject(
            content = value.entries.fold(mutableMapOf()) { acc, v ->
                acc[v.key.toString()] =
                    wrapValue(v.value, path + v.key.toString());
                return@fold acc
            },
            path = path
        )
        else -> throw AssertionError()
    }
}
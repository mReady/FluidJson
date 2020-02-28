package net.mready.json.internal

import net.mready.json.FluidJson
import net.mready.json.JsonAdapter

fun <T> FluidJson.Companion.wrap(value: T, path: JsonPath = JsonPath.ROOT, adapter: JsonAdapter): FluidJson {
    return when (value) {
        null -> JsonNull(path, adapter)
        is String -> JsonPrimitive(value, JsonPrimitive.Type.STRING, path, adapter)
        is Number -> JsonPrimitive(value.toString(), JsonPrimitive.Type.NUMBER, path, adapter)
        is Boolean -> JsonPrimitive(value.toString(), JsonPrimitive.Type.BOOLEAN, path, adapter)
        is JsonElement -> value.copy(path, adapter)
        is Collection<*> -> JsonArray(
            content = value.mapIndexedTo(mutableListOf()) { index, item ->
                wrap(item, path + index, adapter)
            },
            path = path,
            adapter = adapter
        )
        is Map<*, *> -> JsonObject(
            content = value.entries.fold(mutableMapOf()) { acc, v ->
                acc[v.key.toString()] = wrap(v.value, path + v.key.toString(), adapter);
                return@fold acc
            },
            path = path,
            adapter = adapter
        )
        else -> throw IllegalArgumentException("Unsupported value $value")
    }
}
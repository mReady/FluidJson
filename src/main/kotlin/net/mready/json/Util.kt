package net.mready.json

import java.lang.AssertionError

internal inline fun <T> jsonNullOr(value: T?, path: JsonPath, block: (T) -> JsonValue): JsonValue {
    return if (value == null) {
        JsonNull(path)
    } else {
        block(value)
    }
}

internal inline fun <reified T> wrapValue(value: T, path: JsonPath = JsonPath.ROOT): JsonValue {
    return when (value) {
        null -> JsonNull(path)
        is String -> JsonPrimitive(value, JsonPrimitive.Type.STRING, path)
        is Number -> JsonPrimitive(value.toString(), JsonPrimitive.Type.NUMBER, path)
        is Boolean -> JsonPrimitive(value.toString(), JsonPrimitive.Type.BOOLEAN, path)
        is JsonValue -> value.copyWithPath(path)
        else -> throw AssertionError()
    }
}

internal fun wrapArray(collection: Collection<Any?>?, path: JsonPath): JsonValue {
    return jsonNullOr(collection, path) {
        JsonArray(
            it.mapIndexedTo(mutableListOf()) { index, item -> wrapValue(item, path + index) },
            path
        )
    }
}
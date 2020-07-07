package net.mready.json

import net.mready.json.internal.*
import kotlin.reflect.KType

class JsonParseException(message: String, cause: Throwable) : RuntimeException(message, cause)

abstract class JsonAdapter {
    abstract fun parse(string: String): FluidJson
    abstract fun stringify(json: FluidJson): String

    fun newJson(): FluidJson {
        return JsonEmptyElement(adapter = this)
    }

    open fun wrap(value: Any?, path: JsonPath = JsonPath.ROOT): FluidJson {
        return wrapInternal(value, path) ?: throw IllegalArgumentException("Unsupported value $value")
    }

    protected fun wrapInternal(value: Any?, path: JsonPath): FluidJson? {
        return when (value) {
            null -> JsonNullElement(path, this)
            is String -> JsonPrimitiveElement(value, JsonPrimitiveElement.Type.STRING, path, this)
            is Number -> JsonPrimitiveElement(value.toString(), JsonPrimitiveElement.Type.NUMBER, path, this)
            is Boolean -> JsonPrimitiveElement(value.toString(), JsonPrimitiveElement.Type.BOOLEAN, path, this)
            is JsonElement -> value.copy(path, this)
            is Collection<*> -> JsonArrayElement(
                content = value.mapIndexedTo(mutableListOf()) { index, item ->
                    wrap(item, path + index)
                },
                path = path,
                adapter = this
            )
            is Map<*, *> -> JsonObjectElement(
                content = value.entries.fold(mutableMapOf()) { acc, v ->
                    acc[v.key.toString()] = wrap(v.value, path + v.key.toString());
                    return@fold acc
                },
                path = path,
                adapter = this
            )
            else -> null
        }
    }

    @ExperimentalUserTypes
    abstract fun <T : Any> fromJson(json: FluidJson, type: KType): T

    @ExperimentalUserTypes
    abstract fun toJson(value: Any?, type: KType): FluidJson

    @ExperimentalUserTypes
    abstract fun <T : Any> decodeObject(string: String, type: KType): T

    @ExperimentalUserTypes
    abstract fun encodeObject(value: Any?, type: KType): String
}
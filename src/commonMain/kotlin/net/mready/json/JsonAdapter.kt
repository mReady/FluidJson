package net.mready.json

import net.mready.json.internal.*
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class JsonParseException(message: String, cause: Throwable) : RuntimeException(message, cause)

abstract class JsonAdapter(
    private val transformers: Set<JsonTransformer> = setOf()
) {

    abstract fun parse(string: String): FluidJson
    abstract fun stringify(json: FluidJson): String

    fun newJson(): FluidJson {
        return JsonEmptyElement(adapter = this)
    }

    inline fun wrap(value: Nothing?, path: JsonPath = JsonPath.ROOT): FluidJson {
        return JsonNullElement(path, this)
    }
    
    inline fun <reified T> wrap(value: T, path: JsonPath = JsonPath.ROOT): FluidJson {
        return wrapInternal(value, typeOf<T>(), path)
    }

    fun wrapInternal(value: Any?, type: KType, path: JsonPath): FluidJson {
        transformers.mapNotNull { it.transform(value, type, this) }
            .firstOrNull()
            ?.let { return it.copyIfNeeded(path, this) }

        return when (value) {
            null -> JsonNullElement(path, this)
            is JsonElement -> value.copyIfNeeded(path, this)
            is String -> JsonPrimitiveElement(value, JsonPrimitiveElement.Type.STRING, path, this)
            is Number -> JsonPrimitiveElement(value.toString(), JsonPrimitiveElement.Type.NUMBER, path, this)
            is Boolean -> JsonPrimitiveElement(value.toString(), JsonPrimitiveElement.Type.BOOLEAN, path, this)
            else -> JsonRefElement(value, type, adapter = this)
        }
    }


    abstract fun <T> fromJson(json: FluidJson, type: KType): T

    inline fun <reified T> fromJson(json: FluidJson): T = fromJson(json, typeOf<T>())

    abstract fun toJson(value: Any?, type: KType): FluidJson

    inline fun <reified T> toJson(value: T): FluidJson = toJson(value, typeOf<T>())

    abstract fun <T> decodeObject(string: String, type: KType): T

    inline fun <reified T> decodeObject(string: String): T = decodeObject(string, typeOf<T>())

    abstract fun encodeObject(value: Any?, type: KType): String

    inline fun <reified T> encodeObject(value: T): String = encodeObject(value, typeOf<T>())
}
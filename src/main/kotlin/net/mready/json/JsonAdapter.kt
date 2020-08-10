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

    inline fun <reified T> wrap(value: T, path: JsonPath = JsonPath.ROOT): FluidJson {
        return wrapInternal(value, path, typeOf<T>())
    }

    fun wrapInternal(value: Any?, path: JsonPath, type: KType): FluidJson {
        return when (value) {
            null -> JsonNullElement(path, this)
            is JsonElement -> value.copyIfNeeded(path, this)
            is String -> JsonPrimitiveElement(value, JsonPrimitiveElement.Type.STRING, path, this)
            is Number -> JsonPrimitiveElement(value.toString(), JsonPrimitiveElement.Type.NUMBER, path, this)
            is Boolean -> JsonPrimitiveElement(value.toString(), JsonPrimitiveElement.Type.BOOLEAN, path, this)
            else -> {
                transformers.mapNotNull { it.transform(value, this) }.firstOrNull()
                    ?.let { return it.copyIfNeeded(path, this) }

                when(value) {
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
                    else -> throw IllegalArgumentException("Unsupported value $value")
                }
            }
        }
    }


    @ExperimentalUserTypes
    abstract fun <T : Any> fromJson(json: FluidJson, type: KType): T

    @ExperimentalUserTypes
    inline fun <reified T : Any> fromJson(json: FluidJson): T = fromJson(json, typeOf<T>())

    @ExperimentalUserTypes
    abstract fun toJson(value: Any?, type: KType): FluidJson

    @ExperimentalUserTypes
    inline fun <reified T : Any?> toJson(value: T): FluidJson = toJson(value, typeOf<T>())

    @ExperimentalUserTypes
    abstract fun <T : Any> decodeObject(string: String, type: KType): T

    @ExperimentalUserTypes
    inline fun <reified T : Any> decodeObject(string: String): T = decodeObject(string, typeOf<T>())

    @ExperimentalUserTypes
    abstract fun encodeObject(value: Any?, type: KType): String

    @ExperimentalUserTypes
    inline fun <reified T : Any?> encodeObject(value: T): String = encodeObject(value, typeOf<T>())
}
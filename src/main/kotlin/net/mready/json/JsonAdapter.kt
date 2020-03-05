package net.mready.json

import net.mready.json.adapters.KotlinxJsonAdapter
import net.mready.json.internal.*
import kotlin.reflect.KClass

class JsonParseException(message: String, cause: Throwable) : RuntimeException(message, cause)

abstract class JsonAdapter {
    abstract fun parse(string: String): FluidJson
    abstract fun stringify(json: FluidJson): String

    open fun wrap(value: Any?, path: JsonPath): FluidJson {
        return wrapInternal(value, path) ?: throw IllegalArgumentException("Unsupported value $value")
    }

    protected fun wrapInternal(value: Any?, path: JsonPath): FluidJson? {
        return when (value) {
            null -> JsonNull(path, this)
            is String -> JsonPrimitive(value, JsonPrimitive.Type.STRING, path, this)
            is Number -> JsonPrimitive(value.toString(), JsonPrimitive.Type.NUMBER, path, this)
            is Boolean -> JsonPrimitive(value.toString(), JsonPrimitive.Type.BOOLEAN, path, this)
            is JsonElement -> value.copy(path, this)
            is Collection<*> -> JsonArray(
                content = value.mapIndexedTo(mutableListOf()) { index, item ->
                    wrap(item, path + index)
                },
                path = path,
                adapter = this
            )
            is Map<*, *> -> JsonObject(
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
    abstract fun <T : Any> decodeObject(json: FluidJson, cls: KClass<T>): T

    @ExperimentalUserTypes
    abstract fun encodeObject(value: Any?): FluidJson
}

@PublishedApi
internal var defaultJsonAdapter: JsonAdapter = KotlinxJsonAdapter()
    private set

fun FluidJson.Companion.setDefaultAdapter(adapter: JsonAdapter) {
    defaultJsonAdapter = adapter
}

fun FluidJson.Companion.getDefaultAdapter(): JsonAdapter {
    return defaultJsonAdapter
}
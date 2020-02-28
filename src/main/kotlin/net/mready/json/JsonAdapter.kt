package net.mready.json

import net.mready.json.adapters.KotlinxJsonAdapter
import kotlin.reflect.KClass

class JsonParseException(message: String, cause: Throwable) : RuntimeException(message, cause)

interface JsonAdapter {
    fun parse(string: String): FluidJson
    fun stringify(json: FluidJson): String

    @ExperimentalUserTypes
    fun <T : Any> fromJsonTree(cls: KClass<T>, json: FluidJson): T

    @ExperimentalUserTypes
    fun toJsonTree(value: Any?): FluidJson
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
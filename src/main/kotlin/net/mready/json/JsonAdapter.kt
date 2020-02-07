package net.mready.json

import net.mready.json.kotlinx.KotlinxJsonAdapter
import kotlin.reflect.KClass

class JsonParseException(message: String, cause: Throwable) : RuntimeException(message, cause)

interface JsonAdapter {
    fun parse(string: String): FluidJson
    fun stringify(json: FluidJson, prettyPrint: Boolean = false): String

    @ExperimentalUserTypes
    fun <T : Any> fromJsonTree(cls: KClass<T>, json: FluidJson): T

    @ExperimentalUserTypes
    fun toJsonTree(value: Any?): FluidJson
}

internal var defaultJsonAdapter: JsonAdapter = KotlinxJsonAdapter()

fun FluidJson.Companion.setDefaultAdapter(adapter: JsonAdapter) {
    defaultJsonAdapter = adapter
}
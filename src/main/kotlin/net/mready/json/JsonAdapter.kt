package net.mready.json

import net.mready.json.kotlinx.KotlinxJsonAdapter
import kotlin.reflect.KClass

class JsonParseException(message: String, cause: Throwable) : RuntimeException(message, cause)

interface JsonAdapter {
    fun parse(string: String): JsonValue
    fun stringify(json: JsonValue, prettyPrint: Boolean = false): String

    @ExperimentalUserTypes
    fun <T : Any> fromJson(cls: KClass<T>, json: JsonValue): T

    @ExperimentalUserTypes
    fun toJson(value: Any?): JsonValue
}

internal var defaultJsonAdapter: JsonAdapter = KotlinxJsonAdapter()

fun JsonValue.Companion.setDefaultAdapter(adapter: JsonAdapter) {
    defaultJsonAdapter = adapter
}
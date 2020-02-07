package net.mready.json.kotlinx

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.getContextualOrDefault
import net.mready.json.*
import kotlin.reflect.KClass

class KotlinxJsonAdapter(private val serialModule: SerialModule = EmptyModule) : JsonAdapter {
    @Suppress("EXPERIMENTAL_API_USAGE")
    private val jsonConfiguration = JsonConfiguration(
        prettyPrint = false,
        strictMode = false,
        useArrayPolymorphism = true
    )

    override fun parse(string: String): JsonValue {
        val jsonString = string.trim()
        if (jsonString.isEmpty()) {
            return JsonEmpty()
        }

        if (!jsonString.startsWith('{') && !jsonString.startsWith('[')) {
            return JsonPrimitive(jsonString, JsonPrimitive.Type.UNKNOWN)
        }

        try {
            return Json(jsonConfiguration, serialModule).parse(JsonValueSerializer, jsonString)
        } catch (e: Throwable) {
            throw JsonParseException(e.message ?: "Unable to parse JSON", e)
        }
    }

    override fun stringify(json: JsonValue, prettyPrint: Boolean): String {
        val config = jsonConfiguration.copy(prettyPrint = prettyPrint)
        return Json(config, serialModule).stringify(JsonValueSerializer, json)
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    @ExperimentalUserTypes
    override fun <T : Any> fromJson(cls: KClass<T>, json: JsonValue): T {
        return Json(jsonConfiguration, serialModule).fromJson(Json.context.getContextualOrDefault(cls), json.toJsonElement())
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    @ExperimentalUserTypes
    override fun toJson(value: Any?): JsonValue {
        return value?.let { JsonValue.from(Json(jsonConfiguration, serialModule).toJson(value)) } ?: JsonNull()
    }
}
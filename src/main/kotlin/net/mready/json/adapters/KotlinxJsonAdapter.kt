package net.mready.json.adapters

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.getContextualOrDefault
import net.mready.json.*
import net.mready.json.internal.JsonEmpty
import net.mready.json.internal.JsonNull
import net.mready.json.internal.JsonPrimitive
import kotlin.reflect.KClass

class KotlinxJsonAdapter(private val serialModule: SerialModule = EmptyModule) : JsonAdapter {
    @Suppress("EXPERIMENTAL_API_USAGE")
    private val jsonConfiguration = JsonConfiguration(
        prettyPrint = false,
        strictMode = false,
        useArrayPolymorphism = true
    )

    override fun parse(string: String): FluidJson {
        val jsonString = string.trim()
        if (jsonString.isEmpty()) {
            return JsonEmpty()
        }

        if (!jsonString.startsWith('{') && !jsonString.startsWith('[')) {
            return JsonPrimitive(jsonString, JsonPrimitive.Type.UNKNOWN)
        }

        try {
            return Json(jsonConfiguration, serialModule).parse(FluidJsonSerializer, jsonString)
        } catch (e: Throwable) {
            throw JsonParseException(e.message ?: "Unable to parse JSON", e)
        }
    }

    override fun stringify(json: FluidJson, prettyPrint: Boolean): String {
        val config = jsonConfiguration.copy(prettyPrint = prettyPrint)
        return Json(config, serialModule).stringify(FluidJsonSerializer, json)
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    @ExperimentalUserTypes
    override fun <T : Any> fromJsonTree(cls: KClass<T>, json: FluidJson): T {
        return Json(jsonConfiguration, serialModule).fromJson(Json.context.getContextualOrDefault(cls), json.toJsonElement())
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    @ExperimentalUserTypes
    override fun toJsonTree(value: Any?): FluidJson {
        return value?.let { FluidJson.from(Json(jsonConfiguration, serialModule).toJson(value)) } ?: JsonNull()
    }
}
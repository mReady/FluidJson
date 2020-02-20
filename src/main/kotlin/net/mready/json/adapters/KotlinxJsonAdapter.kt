package net.mready.json.adapters

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.getContextualOrDefault
import net.mready.json.ExperimentalUserTypes
import net.mready.json.FluidJson
import net.mready.json.JsonAdapter
import net.mready.json.JsonParseException
import net.mready.json.internal.JsonEmpty
import net.mready.json.internal.JsonNull
import net.mready.json.internal.JsonPath
import net.mready.json.internal.JsonPrimitive
import kotlin.reflect.KClass

typealias KJson = kotlinx.serialization.json.Json

class KotlinxJsonAdapter(private val serialModule: SerialModule = EmptyModule) : JsonAdapter {
    @Suppress("EXPERIMENTAL_API_USAGE")
    private val jsonConfiguration = JsonConfiguration(
        prettyPrint = false,
        strictMode = false,
        useArrayPolymorphism = true
    )

    private val serializationStrategy = FluidJsonSerialization
    private val deserializationStrategy = FluidJsonDeserialization(this)

    override fun parse(string: String): FluidJson {
        val jsonString = string.trim()
        if (jsonString.isEmpty()) {
            return JsonEmpty(JsonPath.ROOT, adapter = this)
        }

        if (!jsonString.startsWith('{') && !jsonString.startsWith('[')) {
            return JsonPrimitive(jsonString, JsonPrimitive.Type.UNKNOWN, JsonPath.ROOT, adapter = this)
        }

        try {
            return KJson(jsonConfiguration, serialModule).parse(deserializationStrategy, jsonString)
        } catch (e: Throwable) {
            throw JsonParseException(e.message ?: "Unable to parse JSON", e)
        }
    }

    override fun stringify(json: FluidJson, prettyPrint: Boolean): String {
        val config = jsonConfiguration.copy(prettyPrint = prettyPrint)
        return KJson(config, serialModule).stringify(serializationStrategy, json)
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    @ExperimentalUserTypes
    override fun <T : Any> fromJsonTree(cls: KClass<T>, json: FluidJson): T {
        return KJson(jsonConfiguration, serialModule).fromJson(
            KJson.context.getContextualOrDefault(cls),
            json.toJsonElement()
        )
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    @ExperimentalUserTypes
    override fun toJsonTree(value: Any?): FluidJson {
        return value?.let {
            val serializer = KJson.context.getContextualOrDefault(value)
            FluidJson.from(
                KJson(jsonConfiguration, serialModule).toJson(serializer, value)
            )
        } ?: JsonNull(adapter = this)
    }
}
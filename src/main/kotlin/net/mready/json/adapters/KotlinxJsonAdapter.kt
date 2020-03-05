@file:Suppress("MemberVisibilityCanBePrivate")

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
import net.mready.json.internal.*
import kotlin.reflect.KClass

typealias KJson = kotlinx.serialization.json.Json

open class KotlinxJsonAdapter(
    private val serialModule: SerialModule = EmptyModule,
    private val config: JsonConfiguration = defaultJsonConfiguration
) : JsonAdapter() {
    companion object {
        @Suppress("EXPERIMENTAL_API_USAGE")
        val defaultJsonConfiguration = JsonConfiguration(
            prettyPrint = false,
            strictMode = false,
            useArrayPolymorphism = true
        )
    }

    protected val serializationStrategy = FluidJsonSerialization
    @Suppress("LeakingThis")
    protected val deserializationStrategy = FluidJsonDeserialization(this)

    protected val jsonSerializer = KJson(config, serialModule)

    override fun parse(string: String): FluidJson {
        val jsonString = string.trim()
        if (jsonString.isEmpty()) {
            return JsonEmpty(JsonPath.ROOT, adapter = this)
        }

        if (!jsonString.startsWith('{') && !jsonString.startsWith('[')) {
            return JsonPrimitive(jsonString, JsonPrimitive.Type.UNKNOWN, JsonPath.ROOT, adapter = this)
        }

        try {
            return jsonSerializer.parse(deserializationStrategy, jsonString)
        } catch (e: Throwable) {
            throw JsonParseException(e.message ?: "Unable to parse JSON", e)
        }
    }

    override fun stringify(json: FluidJson): String {
        return jsonSerializer.stringify(serializationStrategy, json)
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    @ExperimentalUserTypes
    override fun <T : Any> decodeObject(json: FluidJson, cls: KClass<T>): T {
        return jsonSerializer.fromJson(
            jsonSerializer.context.getContextualOrDefault(cls),
            json.toKotlinJsonElement()
        )
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    @ExperimentalUserTypes
    override fun encodeObject(value: Any?): FluidJson {
        return value?.let {
            val serializer = jsonSerializer.context.getContextualOrDefault(value)
            FluidJson.from(jsonSerializer.toJson(serializer, value))
        } ?: JsonNull(adapter = this)
    }
}
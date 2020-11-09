@file:Suppress("MemberVisibilityCanBePrivate")

package net.mready.json.adapters

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import net.mready.json.*
import net.mready.json.internal.JsonEmptyElement
import net.mready.json.internal.JsonNullElement
import net.mready.json.internal.JsonPath
import net.mready.json.internal.JsonPrimitiveElement
import kotlin.reflect.KType

typealias KJson = kotlinx.serialization.json.Json

open class KotlinxJsonAdapter(
    private val jsonSerializer: KJson = defaultSerializer,
    transformers: Set<JsonTransformer> = setOf()
) : JsonAdapter(transformers) {
    companion object {
        val defaultSerializer = Json {
            prettyPrint = false
            ignoreUnknownKeys = true
            useArrayPolymorphism = true
            encodeDefaults = true
        }
    }

    protected val serializationStrategy = FluidJsonSerialization
    @Suppress("LeakingThis")
    protected val deserializationStrategy = FluidJsonDeserialization(this)

    override fun parse(string: String): FluidJson {
        val jsonString = string.trim()
        if (jsonString.isEmpty()) {
            return JsonEmptyElement(JsonPath.ROOT, adapter = this)
        }

        if (!jsonString.startsWith('{') && !jsonString.startsWith('[')) {
            return JsonPrimitiveElement(jsonString, JsonPrimitiveElement.Type.UNKNOWN, JsonPath.ROOT, adapter = this)
        }

        try {
            return jsonSerializer.decodeFromString(deserializationStrategy, jsonString)
        } catch (e: Throwable) {
            throw JsonParseException(e.message ?: "Unable to parse JSON", e)
        }
    }

    override fun stringify(json: FluidJson): String {
        return jsonSerializer.encodeToString(serializationStrategy, json)
    }

    @ExperimentalUserTypes
    override fun <T : Any> fromJson(json: FluidJson, type: KType): T {
        return jsonSerializer.decodeFromJsonElement(
            jsonSerializer.serializersModule.serializer(type) as KSerializer<T>,
            json.toKotlinJsonElement()
        )
    }

    @ExperimentalUserTypes
    override fun toJson(value: Any?, type: KType): FluidJson {
        return value?.let {
            val serializer = jsonSerializer.serializersModule.serializer(type)
            FluidJson.fromKotlinJsonElement(jsonSerializer.encodeToJsonElement(serializer, it))
        } ?: JsonNullElement(adapter = this)
    }

    @ExperimentalUserTypes
    override fun <T : Any> decodeObject(string: String, type: KType): T {
        return jsonSerializer.decodeFromString(
            jsonSerializer.serializersModule.serializer(type) as KSerializer<T>,
            string
        )
    }

    @ExperimentalUserTypes
    override fun encodeObject(value: Any?, type: KType): String {
        return value?.let {
            val serializer = jsonSerializer.serializersModule.serializer(type)
            jsonSerializer.encodeToString(serializer, it)
        } ?: "null"
    }
}
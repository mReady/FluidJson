package net.mready.json.adapters

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.modules.getContextualOrDefault
import net.mready.json.ExperimentalUserTypes
import net.mready.json.FluidJson
import net.mready.json.JsonAdapter
import net.mready.json.internal.*

private typealias KJsonElement = kotlinx.serialization.json.JsonElement
private typealias KJsonNull = kotlinx.serialization.json.JsonNull
private typealias KJsonPrimitive = kotlinx.serialization.json.JsonPrimitive
private typealias KJsonObject = kotlinx.serialization.json.JsonObject
private typealias KJsonArray = kotlinx.serialization.json.JsonArray

fun FluidJson.Companion.fromKotlinJsonElement(jsonElement: KJsonElement): FluidJson = fromJsonElement(jsonElement)
fun FluidJson.toKotlinJsonElement(): KJsonElement = toJsonElement(this)

@OptIn(ExperimentalUserTypes::class)
@JvmName("convertToJsonElement")
private fun toJsonElement(value: FluidJson): KJsonElement {
    if (value !is JsonElement) throw AssertionError()
    return when (value) {
        is JsonNullElement -> KJsonNull
        is JsonPrimitiveElement -> when {
            value.isNumber() -> JsonPrimitive((value.longOrNull ?: value.double) as Number)
            value.isBool() -> JsonPrimitive(value.bool)
            else -> JsonPrimitive(value.string)
        }
        is JsonObjectElement -> KJsonObject(value.content.mapValues {
            toJsonElement(it.value)
        })
        is JsonArrayElement -> KJsonArray(value.content.map {
            toJsonElement(it)
        })
        is JsonEmptyElement -> value.wrapped?.let {
            toJsonElement(it)
        } ?: KJsonNull
        is JsonRefElement -> value.select(
            valueTransform = { toJsonElement(value.adapter.toJson(it, value.type)) },
            jsonTransform = { toJsonElement(it) }
        )
        is JsonErrorElement -> throw AssertionError()
    }
}

private fun fromJsonElement(
    element: KJsonElement,
    path: JsonPath = JsonPath.ROOT,
    adapter: JsonAdapter = FluidJson
): FluidJson {
    return when (element) {
        is KJsonObject -> {
            val content = element.mapValuesTo(mutableMapOf()) {
                fromJsonElement(it.value, path + it.key, adapter)
            }
            JsonObjectElement(content, adapter = adapter)
        }
        is KJsonArray -> {
            val content = element.mapIndexedTo(mutableListOf()) { index, item ->
                fromJsonElement(item, path + index, adapter)
            }
            JsonArrayElement(content, adapter = adapter)
        }
        is KJsonNull -> JsonNullElement(path, adapter)
        is KJsonPrimitive -> JsonPrimitiveElement(
            content = element.content,
            type = if (element.isString) JsonPrimitiveElement.Type.STRING else JsonPrimitiveElement.Type.UNKNOWN,
            path = path,
            adapter = adapter
        )
    }
}

object FluidJsonSerializer : KSerializer<FluidJson> {
    @OptIn(InternalSerializationApi::class)
    override val descriptor = buildSerialDescriptor("FluidJson", PolymorphicKind.SEALED)

    override fun deserialize(decoder: Decoder): FluidJson {
        return FluidJsonDeserialization(FluidJson).deserialize(decoder)
    }

    override fun serialize(encoder: Encoder, value: FluidJson) {
        FluidJsonSerialization.serialize(encoder, value)
    }
}

class FluidJsonDeserialization(private val adapter: JsonAdapter) : DeserializationStrategy<FluidJson> {
    override val descriptor: SerialDescriptor = FluidJsonSerializer.descriptor

    override fun patch(decoder: Decoder, old: FluidJson): FluidJson {
        throw AssertionError()
    }

    override fun deserialize(decoder: Decoder): FluidJson {
        val input = decoder as JsonDecoder
        return fromJsonElement(input.decodeJsonElement(), adapter = adapter)
    }
}

object FluidJsonSerialization : SerializationStrategy<FluidJson> {
    override val descriptor: SerialDescriptor = FluidJsonSerializer.descriptor

    @OptIn(UnsafeSerializationApi::class)
    override fun serialize(encoder: Encoder, value: FluidJson) {
        if (value !is JsonElement) throw AssertionError()
        when (value) {
            is JsonPrimitiveElement -> encoder.encodeSerializableValue(JsonPrimitiveSerializer, value)
            is JsonObjectElement -> encoder.encodeSerializableValue(JsonObjectSerializer, value)
            is JsonArrayElement -> encoder.encodeSerializableValue(JsonArraySerializer, value)
            is JsonNullElement -> encoder.encodeSerializableValue(JsonNullSerializer, value)
            is JsonRefElement -> value.select(
                valueTransform = {
                    val serializer = encoder.serializersModule.getContextualOrDefault<Any>(value.type)
                    encoder.encodeSerializableValue(serializer, it)
                },
                jsonTransform = {
                    serialize(encoder, it)
                }
            )
            is JsonEmptyElement -> value.wrapped?.let {
                serialize(encoder, it)
            } ?: serialize(
                encoder,
                JsonNullElement(value.path, value.adapter)
            )
        }
    }
}

private object JsonObjectSerializer : SerializationStrategy<JsonObjectElement> {
    private val serializer = MapSerializer(String.serializer(), FluidJsonSerializer)
    override val descriptor = serializer.descriptor

    override fun serialize(encoder: Encoder, value: JsonObjectElement) {
        serializer.serialize(encoder, value.content)
    }
}

private object JsonArraySerializer : SerializationStrategy<JsonArrayElement> {
    private val serializer = ListSerializer(FluidJsonSerializer)
    override val descriptor = serializer.descriptor

    override fun serialize(encoder: Encoder, value: JsonArrayElement) {
        serializer.serialize(encoder, value.content)
    }
}

private object JsonNullSerializer : SerializationStrategy<JsonNullElement> {
    @OptIn(InternalSerializationApi::class)
    override val descriptor = buildSerialDescriptor("JsonNull", SerialKind.ENUM)

    override fun serialize(encoder: Encoder, value: JsonNullElement) {
        encoder.encodeNull()
    }
}

private object JsonPrimitiveSerializer : SerializationStrategy<JsonPrimitiveElement> {
    override val descriptor = PrimitiveSerialDescriptor("JsonPrimitive", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: JsonPrimitiveElement) {
        when {
            value.isNumber() -> value.longOrNull?.let { encoder.encodeLong(it) }
                ?: encoder.encodeDouble(value.double)
            value.isBool() -> encoder.encodeBoolean(value.bool)
            else -> encoder.encodeString(value.string)
        }
    }
}
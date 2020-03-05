package net.mready.json.adapters

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonInput
import kotlinx.serialization.modules.getContextualOrDefault
import net.mready.json.ExperimentalUserTypes
import net.mready.json.FluidJson
import net.mready.json.JsonAdapter
import net.mready.json.defaultJsonAdapter
import net.mready.json.internal.*

private typealias KJsonElement = kotlinx.serialization.json.JsonElement
private typealias KJsonNull = kotlinx.serialization.json.JsonNull
private typealias KJsonLiteral = kotlinx.serialization.json.JsonLiteral
private typealias KJsonObject = kotlinx.serialization.json.JsonObject
private typealias KJsonArray = kotlinx.serialization.json.JsonArray

fun FluidJson.Companion.from(jsonElement: KJsonElement): FluidJson = fromJsonElement(jsonElement)
fun FluidJson.toKotlinJsonElement(): KJsonElement = toJsonElement(this)

@OptIn(ExperimentalUserTypes::class)
@JvmName("convertToJsonElement")
private fun toJsonElement(value: FluidJson): KJsonElement {
    if (value !is JsonElement) throw AssertionError()
    return when (value) {
        is JsonNull -> KJsonNull
        is JsonPrimitive -> when {
            value.isNumber() -> KJsonLiteral((value.longOrNull ?: value.double) as Number)
            value.isBool() -> KJsonLiteral(value.bool)
            else -> KJsonLiteral(value.string)
        }
        is JsonObject -> KJsonObject(value.content.mapValues {
            toJsonElement(it.value)
        })
        is JsonArray -> KJsonArray(value.content.map {
            toJsonElement(it)
        })
        is JsonEmpty -> value.wrapped?.let {
            toJsonElement(it)
        } ?: KJsonNull
        is JsonReference -> value.select(
            valueTransform = { toJsonElement(value.adapter.encodeObject(it)) },
            jsonTransform = { toJsonElement(it) }
        )
        is JsonError -> TODO()
    }
}

private fun fromJsonElement(
    element: KJsonElement,
    path: JsonPath = JsonPath.ROOT,
    adapter: JsonAdapter = defaultJsonAdapter
): FluidJson {
    return when (element) {
        is KJsonObject -> {
            val content = element.content.mapValuesTo(mutableMapOf()) {
                fromJsonElement(it.value, path + it.key, adapter)
            }
            JsonObject(content, adapter = adapter)
        }
        is KJsonArray -> {
            val content = element.content.mapIndexedTo(mutableListOf()) { index, item ->
                fromJsonElement(item, path + index, adapter)
            }
            JsonArray(content, adapter = adapter)
        }
        is KJsonNull -> JsonNull(path, adapter)
        is KJsonLiteral -> JsonPrimitive(
            content = element.content,
            type = if (element.isString) JsonPrimitive.Type.STRING else JsonPrimitive.Type.UNKNOWN,
            path = path,
            adapter = adapter
        )
    }
}

object FluidJsonSerializer : KSerializer<FluidJson> {
    override val descriptor = SerialDescriptor("FluidJson", PolymorphicKind.SEALED)

    override fun deserialize(decoder: Decoder): FluidJson {
        return FluidJsonDeserialization(defaultJsonAdapter).deserialize(decoder)
    }

    override fun serialize(encoder: Encoder, value: FluidJson) {
        FluidJsonSerialization.serialize(encoder, value)
    }
}

class FluidJsonDeserialization(private val adapter: JsonAdapter) : DeserializationStrategy<FluidJson> {
    override val descriptor: SerialDescriptor = FluidJsonSerializer.descriptor

    override fun patch(decoder: Decoder, old: FluidJson) = throw UpdateNotSupportedException(descriptor.serialName)

    override fun deserialize(decoder: Decoder): FluidJson {
        val input = decoder as JsonInput
        return fromJsonElement(input.decodeJson(), adapter = adapter)
    }
}

object FluidJsonSerialization : SerializationStrategy<FluidJson> {
    override val descriptor: SerialDescriptor = FluidJsonSerializer.descriptor

    @OptIn(ImplicitReflectionSerializer::class)
    override fun serialize(encoder: Encoder, value: FluidJson) {
        if (value !is JsonElement) throw AssertionError()
        when (value) {
            is JsonPrimitive -> encoder.encodeSerializableValue(JsonPrimitiveSerializer, value)
            is JsonObject -> encoder.encodeSerializableValue(JsonObjectSerializer, value)
            is JsonArray -> encoder.encodeSerializableValue(JsonArraySerializer, value)
            is JsonNull -> encoder.encodeSerializableValue(JsonNullSerializer, value)
            is JsonReference -> value.select(
                valueTransform = {
                    encoder.encodeSerializableValue(encoder.context.getContextualOrDefault(it), it)
                },
                jsonTransform = {
                    serialize(encoder, it)
                }
            )
            is JsonEmpty -> value.wrapped?.let {
                serialize(encoder, it)
            } ?: serialize(
                encoder,
                JsonNull(value.path, value.adapter)
            )
        }
    }
}

private object JsonObjectSerializer : SerializationStrategy<JsonObject> {
    override val descriptor = SerialDescriptor("JsonObject", StructureKind.MAP) {
        mapDescriptor(
            keyDescriptor = String.serializer().descriptor,
            valueDescriptor = FluidJsonSerializer.descriptor
        )
    }

    override fun serialize(encoder: Encoder, value: JsonObject) {
        MapSerializer(String.serializer(), FluidJsonSerializer).serialize(encoder, value.content)
    }
}

private object JsonArraySerializer : SerializationStrategy<JsonArray> {
    override val descriptor = SerialDescriptor("JsonArray", StructureKind.LIST) {
        listDescriptor(typeDescriptor = FluidJsonSerializer.descriptor)
    }

    override fun serialize(encoder: Encoder, value: JsonArray) {
        ListSerializer(FluidJsonSerializer).serialize(encoder, value.content)
    }
}

private object JsonNullSerializer : SerializationStrategy<JsonNull> {
    override val descriptor = SerialDescriptor("JsonNull", UnionKind.ENUM_KIND)

    override fun serialize(encoder: Encoder, value: JsonNull) {
        encoder.encodeNull()
    }
}

private object JsonPrimitiveSerializer : SerializationStrategy<JsonPrimitive> {
    override val descriptor = SerialDescriptor("JsonPrimitive", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: JsonPrimitive) {
        when {
            value.isNumber() -> value.longOrNull?.let { encoder.encodeLong(it) }
                ?: encoder.encodeDouble(value.double)
            value.isBool() -> encoder.encodeBoolean(value.bool)
            else -> encoder.encodeString(value.string)
        }
    }
}
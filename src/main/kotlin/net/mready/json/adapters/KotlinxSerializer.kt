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
import net.mready.json.internal.*

private typealias KJsonElement = kotlinx.serialization.json.JsonElement
private typealias KJsonNull = kotlinx.serialization.json.JsonNull
private typealias KJsonLiteral = kotlinx.serialization.json.JsonLiteral
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
            value.isNumber() -> KJsonLiteral((value.longOrNull ?: value.double) as Number)
            value.isBool() -> KJsonLiteral(value.bool)
            else -> KJsonLiteral(value.string)
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
            val content = element.content.mapValuesTo(mutableMapOf()) {
                fromJsonElement(it.value, path + it.key, adapter)
            }
            JsonObjectElement(content, adapter = adapter)
        }
        is KJsonArray -> {
            val content = element.content.mapIndexedTo(mutableListOf()) { index, item ->
                fromJsonElement(item, path + index, adapter)
            }
            JsonArrayElement(content, adapter = adapter)
        }
        is KJsonNull -> JsonNullElement(path, adapter)
        is KJsonLiteral -> JsonPrimitiveElement(
            content = element.content,
            type = if (element.isString) JsonPrimitiveElement.Type.STRING else JsonPrimitiveElement.Type.UNKNOWN,
            path = path,
            adapter = adapter
        )
    }
}

object FluidJsonSerializer : KSerializer<FluidJson> {
    override val descriptor = SerialDescriptor("FluidJson", PolymorphicKind.SEALED)

    override fun deserialize(decoder: Decoder): FluidJson {
        return FluidJsonDeserialization(FluidJson).deserialize(decoder)
    }

    override fun serialize(encoder: Encoder, value: FluidJson) {
        FluidJsonSerialization.serialize(encoder, value)
    }
}

class FluidJsonDeserialization(private val adapter: JsonAdapter) : DeserializationStrategy<FluidJson> {
    override val descriptor: SerialDescriptor = FluidJsonSerializer.descriptor

    override fun patch(decoder: Decoder, old: FluidJson) = throw AssertionError()

    override fun deserialize(decoder: Decoder): FluidJson {
        val input = decoder as JsonInput
        return fromJsonElement(input.decodeJson(), adapter = adapter)
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
                    val serializer = encoder.context.getContextualOrDefault<Any>(value.type)
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
    override val descriptor = SerialDescriptor("JsonObject", StructureKind.MAP) {
        mapDescriptor(
            keyDescriptor = String.serializer().descriptor,
            valueDescriptor = FluidJsonSerializer.descriptor
        )
    }

    override fun serialize(encoder: Encoder, value: JsonObjectElement) {
        MapSerializer(String.serializer(), FluidJsonSerializer).serialize(encoder, value.content)
    }
}

private object JsonArraySerializer : SerializationStrategy<JsonArrayElement> {
    override val descriptor = SerialDescriptor("JsonArray", StructureKind.LIST) {
        listDescriptor(typeDescriptor = FluidJsonSerializer.descriptor)
    }

    override fun serialize(encoder: Encoder, value: JsonArrayElement) {
        ListSerializer(FluidJsonSerializer).serialize(encoder, value.content)
    }
}

private object JsonNullSerializer : SerializationStrategy<JsonNullElement> {
    override val descriptor = SerialDescriptor("JsonNull", UnionKind.ENUM_KIND)

    override fun serialize(encoder: Encoder, value: JsonNullElement) {
        encoder.encodeNull()
    }
}

private object JsonPrimitiveSerializer : SerializationStrategy<JsonPrimitiveElement> {
    override val descriptor = SerialDescriptor("JsonPrimitive", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: JsonPrimitiveElement) {
        when {
            value.isNumber() -> value.longOrNull?.let { encoder.encodeLong(it) }
                ?: encoder.encodeDouble(value.double)
            value.isBool() -> encoder.encodeBoolean(value.bool)
            else -> encoder.encodeString(value.string)
        }
    }
}
package net.mready.json.adapters

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import net.mready.json.*
import net.mready.json.internal.*
import kotlin.reflect.KType
import kotlin.reflect.javaType

object JacksonJsonAdapter : JsonAdapter() {
    private val module = SimpleModule(
        "JsonModule",
        Version.unknownVersion(),
        mapOf(Json::class.java to JsonElementDeserializer(this)),
        listOf(JsonElementSerializer)
    )
    private val objectMapper = jacksonObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .registerModule(module)

    override fun parse(string: String): FluidJson {
        if (string.isEmpty()) {
            return JsonEmptyElement(adapter = this)
        }

        if (!string.startsWith('{') && !string.startsWith('[')) {
            return JsonPrimitiveElement(string, JsonPrimitiveElement.Type.UNKNOWN, JsonPath.ROOT, adapter = this)
        }

        try {
            return objectMapper.readValue(string)
        } catch (e: Throwable) {
            throw JsonParseException(e.message ?: "Unable to parse JSON", e)
        }
    }

    override fun stringify(json: FluidJson): String {
        return objectMapper.writeValueAsString(json)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @ExperimentalUserTypes
    override fun <T : Any> decodeObject(json: FluidJson, type: KType): T {
        return objectMapper.readValue<T>(stringify(json), objectMapper.constructType(type.javaType))
    }

    @ExperimentalUserTypes
    override fun encodeObject(value: Any?, type: KType): FluidJson {
        return parse(objectMapper.writeValueAsString(value))
    }
}


object JsonElementSerializer : JsonSerializer<Json>() {
    override fun handledType(): Class<Json> {
        return Json::class.java
    }

    override fun serialize(value: Json, gen: JsonGenerator, serializers: SerializerProvider) {
        require(value is JsonElement)

        when (value) {
            is JsonNullElement -> gen.writeNull()
            is JsonPrimitiveElement -> when {
                value.isBool() -> gen.writeBoolean(value.bool)
                value.isNumber() -> value.longOrNull?.let { gen.writeNumber(it) } ?: gen.writeNumber(value.double)
                else -> gen.writeString(value.string)
            }
            is JsonObjectElement -> {
                gen.writeStartObject()
                value.obj.forEach {
                    gen.writeFieldName(it.key)
                    serialize(it.value, gen, serializers)
                }
                gen.writeEndObject()
            }
            is JsonArrayElement -> {
                gen.writeStartArray()
                value.array.forEach {
                    serialize(it, gen, serializers)
                }
                gen.writeEndArray()
            }
            is JsonRefElement -> value.select(
                valueTransform = {
                    serializers.findValueSerializer(serializers.constructType(value.type.javaType))
                        .serialize(it, gen, serializers)
                },
                jsonTransform = {
                    serialize(it, gen, serializers)
                }
            )
            is JsonEmptyElement -> value.wrapped?.let {
                serialize(it, gen, serializers)
            } ?: gen.writeNull()
            is JsonErrorElement -> throw AssertionError()
        }
    }
}

class JsonElementDeserializer(private val adapter: JsonAdapter) : JsonDeserializer<Json>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Json {
        return parse(p.currentToken, p, JsonPath.ROOT)
    }

    private fun parse(token: JsonToken, p: JsonParser, path: JsonPath): JsonElement {
        return when (token) {
            JsonToken.VALUE_NULL -> JsonNullElement(path = path, adapter = adapter)
            JsonToken.VALUE_STRING -> JsonPrimitiveElement(
                p.text,
                JsonPrimitiveElement.Type.STRING,
                path = path,
                adapter = adapter
            )
            JsonToken.VALUE_NUMBER_INT -> JsonPrimitiveElement(
                p.longValue.toString(),
                JsonPrimitiveElement.Type.NUMBER,
                path = path,
                adapter = adapter
            )
            JsonToken.VALUE_NUMBER_FLOAT -> JsonPrimitiveElement(
                p.doubleValue.toString(),
                JsonPrimitiveElement.Type.NUMBER,
                path = path,
                adapter = adapter
            )
            JsonToken.VALUE_TRUE, JsonToken.VALUE_FALSE -> JsonPrimitiveElement(
                p.booleanValue.toString(),
                JsonPrimitiveElement.Type.BOOLEAN,
                path = path,
                adapter = adapter
            )
            JsonToken.START_ARRAY -> {
                val items = mutableListOf<Json>()
                var t = p.nextToken()
                var index = 0
                while (t != JsonToken.END_ARRAY) {
                    items += parse(t, p, path + index)
                    index++
                    t = p.nextToken()
                }
                JsonArrayElement(items, path = path, adapter = adapter)
            }
            JsonToken.START_OBJECT -> {
                val items = mutableMapOf<String, Json>()
                var t = p.nextToken()
                while (t != JsonToken.END_OBJECT) {
                    val key = p.currentName
                    t = p.nextToken()
                    items[key] = parse(t, p, path + key)
                    t = p.nextToken()
                }
                JsonObjectElement(items, path = path, adapter = adapter)
            }
            else -> throw IllegalStateException("Unexpected token $token")
        }
    }
}
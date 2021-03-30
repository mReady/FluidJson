package net.mready.json.adapters

import kotlinx.serialization.*
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KType


@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
internal fun findClassSerializer(serializersModule: SerializersModule, value: Any): KSerializer<Any?>? {
    @Suppress("UNCHECKED_CAST")
    return (value::class.serializerOrNull()
        ?: serializersModule.getContextual(value::class)) as KSerializer<Any?>?
}

internal fun findSerializer(serializersModule: SerializersModule, type: KType, value: Any): KSerializer<Any?>? {
    return if (type.classifier == Any::class) {
        findClassSerializer(serializersModule, value)
    } else {
        runCatching { serializersModule.serializer(type) }
            .getOrElse { findClassSerializer(serializersModule, value) }
    }
}
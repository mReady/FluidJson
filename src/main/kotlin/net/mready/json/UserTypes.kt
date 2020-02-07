package net.mready.json

import kotlin.reflect.KClass

@Experimental
annotation class ExperimentalUserTypes

@ExperimentalUserTypes
inline fun <reified T : Any> JsonAdapter.fromJson(json: JsonValue): T = fromJson(T::class, json)

@ExperimentalUserTypes
fun JsonValue.Companion.wrap(value: Any?): JsonValue = jsonNullOr(value, JsonPath.ROOT) { JsonReference(it) }

@ExperimentalUserTypes
inline fun <reified T: Any> JsonValue.valueOrNull(): T? = valueOrNull(T::class)

@ExperimentalUserTypes
inline fun <reified T: Any> JsonValue.value(): T = value(T::class)

@Suppress("UNCHECKED_CAST")
@ExperimentalUserTypes
fun <T: Any> JsonValue.valueOrNull(cls: KClass<T>): T? = when(this) {
    is JsonNull -> null
    is JsonError -> null
    is JsonReference -> content as? T
    is JsonArray, is JsonObject, is JsonPrimitive -> defaultJsonAdapter.fromJson(cls, this)
    is JsonEmpty -> wrapped?.valueOrNull(cls)
}

@ExperimentalUserTypes
fun <T: Any> JsonValue.value(cls: KClass<T>): T = valueOrNull(cls) ?: throwInvalidType(cls.simpleName.orEmpty())


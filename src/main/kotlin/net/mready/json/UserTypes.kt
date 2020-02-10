package net.mready.json

import net.mready.json.internal.*
import kotlin.reflect.KClass

@Experimental
annotation class ExperimentalUserTypes

@ExperimentalUserTypes
inline fun <reified T : Any> JsonAdapter.fromJsonTree(json: FluidJson): T = fromJsonTree(T::class, json)

@ExperimentalUserTypes
fun FluidJson.Companion.wrap(value: Any?, adapter: JsonAdapter = defaultJsonAdapter): FluidJson =
    jsonNullOr(value, JsonPath.ROOT, adapter) {
        JsonReference(it, adapter = adapter)
    }

@ExperimentalUserTypes
inline fun <reified T: Any> FluidJson.valueOrNull(): T? = valueOrNull(T::class)

@ExperimentalUserTypes
inline fun <reified T: Any> FluidJson.value(): T = value(T::class)

@Suppress("UNCHECKED_CAST")
@ExperimentalUserTypes
fun <T: Any> FluidJson.valueOrNull(cls: KClass<T>): T? {
    if (this !is JsonElement) throw AssertionError()
    return when(this) {
        is JsonNull -> null
        is JsonError -> null
        is JsonReference -> content as? T
        is JsonArray, is JsonObject, is JsonPrimitive -> adapter.fromJsonTree(cls, this)
        is JsonEmpty -> wrapped?.valueOrNull(cls)
    }
}

@ExperimentalUserTypes
fun <T: Any> FluidJson.value(cls: KClass<T>): T = valueOrNull(cls) ?: (this as JsonElement).throwInvalidType(cls.simpleName.orEmpty())


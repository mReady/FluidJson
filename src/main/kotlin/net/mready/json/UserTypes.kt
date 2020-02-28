package net.mready.json

import net.mready.json.internal.*

@Experimental
annotation class ExperimentalUserTypes

@ExperimentalUserTypes
inline fun <reified T : Any> JsonAdapter.fromJsonTree(json: FluidJson): T = fromJsonTree(T::class, json)

@ExperimentalUserTypes
fun FluidJson.Companion.ref(
    value: Any?,
    path: JsonPath = JsonPath.ROOT,
    adapter: JsonAdapter = defaultJsonAdapter
): FluidJson =
    value?.let { JsonReference(it, adapter = adapter) } ?: JsonNull(path, adapter)

@ExperimentalUserTypes
inline fun <reified T : Any> FluidJson.valueOrNull(): T? {
    require(this is JsonElement)
    return when (this) {
        is JsonNull -> null
        is JsonError -> null
        is JsonReference -> select(
            valueTransform = { it as? T },
            jsonTransform = { runCatching { adapter.fromJsonTree(T::class, it) }.getOrNull() }
        )
        is JsonArray, is JsonObject, is JsonPrimitive -> runCatching {
            adapter.fromJsonTree(T::class, this)
        }.getOrNull()
        is JsonEmpty -> when (val wrapped = wrapped) {
            null -> null
            else -> runCatching { adapter.fromJsonTree(T::class, wrapped) }.getOrNull()
        }
    }
}

@ExperimentalUserTypes
inline fun <reified T : Any> FluidJson.value(): T =
    valueOrNull() ?: (this as JsonElement).throwInvalidType(T::class.simpleName.orEmpty())


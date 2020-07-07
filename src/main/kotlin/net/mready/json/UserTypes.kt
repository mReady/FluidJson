package net.mready.json

import net.mready.json.internal.*
import kotlin.reflect.typeOf

@RequiresOptIn
annotation class ExperimentalUserTypes

@ExperimentalUserTypes
inline fun <reified T : Any> JsonAdapter.decodeObject(json: FluidJson): T = decodeObject(json, typeOf<T>())

@ExperimentalUserTypes
inline fun <reified T: Any> FluidJson.Companion.ref(
    value: T?,
    path: JsonPath = JsonPath.ROOT,
    adapter: JsonAdapter = defaultJsonAdapter
): FluidJson =
    value?.let { JsonRefElement(it, typeOf<T>(), adapter = adapter) } ?: JsonNullElement(path, adapter)

@ExperimentalUserTypes
inline fun <reified T : Any> FluidJson.valueOrNull(): T? {
    require(this is JsonElement)
    return when (this) {
        is JsonNullElement -> null
        is JsonErrorElement -> null
        is JsonRefElement -> select(
            valueTransform = { it as? T },
            jsonTransform = { runCatching { adapter.decodeObject<T>(it, typeOf<T>()) }.getOrNull() }
        )
        is JsonArrayElement, is JsonObjectElement, is JsonPrimitiveElement -> runCatching {
            adapter.decodeObject<T>(this, typeOf<T>())
        }.getOrNull()
        is JsonEmptyElement -> when (val wrapped = wrapped) {
            null -> null
            else -> runCatching { adapter.decodeObject<T>(wrapped, typeOf<T>()) }.getOrNull()
        }
    }
}

@ExperimentalUserTypes
inline fun <reified T : Any> FluidJson.value(): T {
    require(this is JsonElement)
    return when (this) {
        is JsonNullElement -> null
        is JsonErrorElement -> null
        is JsonRefElement -> select(
            valueTransform = { it as? T },
            jsonTransform = { runCatching { adapter.decodeObject<T>(it, typeOf<T>()) }.getOrNull() }
        )
        is JsonArrayElement,
        is JsonObjectElement,
        is JsonPrimitiveElement
        -> adapter.decodeObject<T>(this, typeOf<T>())

        is JsonEmptyElement -> when (val wrapped = wrapped) {
            null -> null
            else -> adapter.decodeObject<T>(wrapped, typeOf<T>())
        }
    } ?: this.throwInvalidType(T::class.simpleName.orEmpty())
}
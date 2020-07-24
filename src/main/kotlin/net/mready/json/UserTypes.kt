package net.mready.json

import net.mready.json.internal.*
import kotlin.reflect.typeOf

@RequiresOptIn
annotation class ExperimentalUserTypes

@ExperimentalUserTypes
inline fun <reified T : Any> JsonAdapter.ref(
    value: T?,
    path: JsonPath = JsonPath.ROOT,
): FluidJson =
    value?.let { JsonRefElement(it, typeOf<T>(), adapter = this) } ?: JsonNullElement(path, this)

@ExperimentalUserTypes
inline fun <reified T : Any> FluidJson.valueOrNull(): T? {
    require(this is JsonElement)
    return when (this) {
        is JsonNullElement -> null
        is JsonErrorElement -> null
        is JsonRefElement -> select(
            valueTransform = { it as? T },
            jsonTransform = { runCatching { adapter.fromJson<T>(it, typeOf<T>()) }.getOrNull() }
        )
        is JsonArrayElement, is JsonObjectElement, is JsonPrimitiveElement -> runCatching {
            adapter.fromJson<T>(this, typeOf<T>())
        }.getOrNull()
        is JsonEmptyElement -> when (val wrapped = wrapped) {
            null -> null
            else -> runCatching { adapter.fromJson<T>(wrapped, typeOf<T>()) }.getOrNull()
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
            jsonTransform = { runCatching { adapter.fromJson<T>(it, typeOf<T>()) }.getOrNull() }
        )
        is JsonArrayElement,
        is JsonObjectElement,
        is JsonPrimitiveElement
        -> adapter.fromJson<T>(this, typeOf<T>())

        is JsonEmptyElement -> when (val wrapped = wrapped) {
            null -> null
            else -> adapter.fromJson<T>(wrapped, typeOf<T>())
        }
    } ?: this.throwInvalidType(T::class.simpleName.orEmpty())
}
package net.mready.json.internal

import kotlinx.atomicfu.atomic
import net.mready.json.FluidJson
import net.mready.json.FluidJsonException
import net.mready.json.JsonAdapter
import kotlin.js.JsName

class JsonEmptyElement(
    path: JsonPath = JsonPath.ROOT,
    adapter: JsonAdapter,
    private val pendingException: (() -> FluidJsonException)? = null
) : JsonElement(path, adapter) {
    override val elementName: String
        get() = wrapped.value?.elementName ?: "null"

    private val wrapped = atomic<JsonElement?>(null)
    private inline val defaultException get() = FluidJsonException("Json element is empty", path)

    @JsName("getWrapped")
    fun wrapped(): FluidJson? = wrapped.value

    override fun copy(path: JsonPath, adapter: JsonAdapter) =
        wrapped.value?.copy(path, adapter) ?: JsonEmptyElement(path, adapter, pendingException)

    override val size: Int get() = wrapped.value?.size ?: 0

    private fun materializeAsObject(): FluidJson {
        wrapped.compareAndSet(null, JsonObjectElement(mutableMapOf(), path, adapter))
        if (wrapped.value !is JsonObjectElement) {
            throwInvalidType("object")
        } else {
            return wrapped.value!!
        }
    }

    private fun materializeAsArray(): FluidJson {
        wrapped.compareAndSet(null, JsonArrayElement(mutableListOf(), path, adapter))
        if (wrapped.value !is JsonArrayElement) {
            throwInvalidType("array")
        } else {
            return wrapped.value!!
        }
    }

    override operator fun get(key: String): FluidJson {
        return materializeAsObject()[key]
    }

    override operator fun set(key: String, value: FluidJson?) {
        materializeAsObject()[key] = value
    }

    override operator fun get(index: Int): FluidJson {
        return materializeAsArray()[index]
    }

    override operator fun set(index: Int, value: FluidJson?) {
        materializeAsArray()[index] = value
    }

    override operator fun plusAssign(value: FluidJson?) {
        materializeAsArray() += value
    }

    override fun delete(key: String) = materializeAsObject().delete(key)
    override fun delete(index: Int) = materializeAsArray().delete(index)

    override val isNull: Boolean get() = when(val wrapped = wrapped.value) {
        null -> true
        is JsonObjectElement -> wrapped.obj.values
            .all { it is JsonEmptyElement && it.wrapped()?.isNull != false }
        is JsonArrayElement -> wrapped.array
            .all { it is JsonEmptyElement && it.wrapped()?.isNull != false }
        else -> false
    }
    override val orNull: FluidJson? get() = if (isNull) null else wrapped.value

    override val string: String get() = throwError(pendingException?.invoke() ?: defaultException)
    override val int: Int get() = throwError(pendingException?.invoke() ?: defaultException)
    override val long: Long get() = throwError(pendingException?.invoke() ?: defaultException)
    override val double: Double get() = throwError(pendingException?.invoke() ?: defaultException)
    override val bool: Boolean get() = throwError(pendingException?.invoke() ?: defaultException)

    override val arrayOrNull: List<FluidJson>? get() = orNull?.arrayOrNull
    override val array: List<FluidJson>
        get() = wrapped.value?.array ?: throwError(pendingException?.invoke() ?: defaultException)

    override val objOrNull: Map<String, FluidJson>? get() = orNull?.objOrNull
    override val obj: Map<String, FluidJson>
        get() = wrapped.value?.obj ?: throwError(pendingException?.invoke() ?: defaultException)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is JsonEmptyElement) {
            return this.wrapped.value == other.wrapped.value
        }
        if (other is JsonObjectElement || other is JsonArrayElement) {
            return this.wrapped.value == other
        }
        return false
    }

    override fun hashCode(): Int {
        return wrapped.value?.hashCode() ?: 0
    }
}
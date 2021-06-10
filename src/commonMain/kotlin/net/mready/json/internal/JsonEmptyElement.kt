package net.mready.json.internal

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
        get() = wrapped?.elementName ?: "null"

    @PublishedApi
    internal var wrapped: JsonElement? = null
    private inline val defaultException get() = FluidJsonException("Json element is empty", path)

    @JsName("getWrapped")
    fun wrapped(): FluidJson? = wrapped

    override fun copy(path: JsonPath, adapter: JsonAdapter) =
        wrapped?.copy(path, adapter) ?: JsonEmptyElement(path, adapter, pendingException)

    override val size: Int get() = wrapped?.size ?: 0

    private fun materializeAsObject(): FluidJson {
        if (wrapped == null) {
            // TODO
            //synchronized(this) {
                if (wrapped !is JsonObjectElement?) throwInvalidType("object")
                wrapped = JsonObjectElement(mutableMapOf(), path, adapter)
            //}
        } else if (wrapped !is JsonObjectElement) {
            throwInvalidType("object")
        }

        return wrapped!!
    }

    private fun materializeAsArray(): FluidJson {
        if (wrapped == null) {
            // TODO
            // synchronized(this) {
                if (wrapped !is JsonArrayElement?) throwInvalidType("array")
                wrapped = JsonArrayElement(mutableListOf(), path, adapter)
            //}
        } else if (wrapped !is JsonArrayElement) {
            throwInvalidType("array")
        }

        return wrapped!!
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

    override val isNull: Boolean get() = wrapped == null
    override val orNull: FluidJson? get() = wrapped

    override val string: String get() = throwError(pendingException?.invoke() ?: defaultException)
    override val int: Int get() = throwError(pendingException?.invoke() ?: defaultException)
    override val long: Long get() = throwError(pendingException?.invoke() ?: defaultException)
    override val double: Double get() = throwError(pendingException?.invoke() ?: defaultException)
    override val bool: Boolean get() = throwError(pendingException?.invoke() ?: defaultException)

    override val arrayOrNull: List<FluidJson>? get() = wrapped?.arrayOrNull
    override val array: List<FluidJson>
        get() = wrapped?.array ?: throwError(pendingException?.invoke() ?: defaultException)

    override val objOrNull: Map<String, FluidJson>? get() = wrapped?.objOrNull
    override val obj: Map<String, FluidJson>
        get() = wrapped?.obj ?: throwError(pendingException?.invoke() ?: defaultException)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is JsonEmptyElement) {
            return this.wrapped == other.wrapped
        }
        if (other is JsonObjectElement || other is JsonArrayElement) {
            return this.wrapped == other
        }
        return false
    }

    override fun hashCode(): Int {
        return wrapped?.hashCode() ?: 0
    }
}
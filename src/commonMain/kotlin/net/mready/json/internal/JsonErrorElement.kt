package net.mready.json.internal

import net.mready.json.FluidJsonException
import net.mready.json.JsonAdapter

class JsonErrorElement(
    private val e: FluidJsonException,
    path: JsonPath = JsonPath.ROOT,
    adapter: JsonAdapter
) : JsonElement(path, adapter) {
    override val elementName: String
        get() = "error (${e.message})"

    override fun copy(path: JsonPath, adapter: JsonAdapter) = JsonErrorElement(e, path, adapter)

    fun throwError(): Nothing = throwError(e)

    override operator fun get(key: String) = this
    override operator fun get(index: Int) = this

    override val isNull: Boolean get() = true
    override val orNull: JsonElement? get() = null

    override val string: String get() = throwError()
    override val int: Int get() = throwError()
    override val long: Long get() = throwError()
    override val double: Double get() = throwError()
    override val bool: Boolean get() = throwError()
    override val array: List<JsonElement> get() = throwError()
    override val obj: Map<String, JsonElement> get() = throwError()
}
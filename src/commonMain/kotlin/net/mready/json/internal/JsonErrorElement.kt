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

    override operator fun get(key: String) = this
    override operator fun get(index: Int) = this

    override val isNull: Boolean get() = true
    override val orNull: JsonElement? get() = null

    override val string: String get() = throwError(e)
    override val int: Int get() = throwError(e)
    override val long: Long get() = throwError(e)
    override val double: Double get() = throwError(e)
    override val bool: Boolean get() = throwError(e)
    override val array: List<JsonElement> get() = throwError(e)
    override val obj: Map<String, JsonElement> get() = throwError(e)
}
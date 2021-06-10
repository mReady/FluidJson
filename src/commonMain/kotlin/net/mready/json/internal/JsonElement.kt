package net.mready.json.internal

import net.mready.json.FluidJson
import net.mready.json.FluidJsonException
import net.mready.json.JsonAdapter


sealed class JsonElement(path: JsonPath, adapter: JsonAdapter) : FluidJson(path, adapter) {
    abstract val elementName: String

    @Suppress("NOTHING_TO_INLINE")
    protected inline fun throwError(e: FluidJsonException): Nothing {
        throw e
    }

    @PublishedApi
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun throwInvalidType(expected: String): Nothing {
        throw FluidJsonException("Element $elementName is not $expected", path)
    }

    override fun copyIfNeeded(path: JsonPath, adapter: JsonAdapter): FluidJson {
        return if (path != this.path || adapter != this.adapter) {
            copy(path, adapter)
        } else {
            this
        }
    }

    override operator fun get(key: String): FluidJson {
        return JsonErrorElement(
            e = FluidJsonException("Element $elementName is not an object", path),
            path = path + key,
            adapter = adapter
        )
    }

    override operator fun get(index: Int): FluidJson {
        return JsonErrorElement(
            e = FluidJsonException("Element $elementName is not an array", path),
            path = path + index,
            adapter = adapter
        )
    }

    override operator fun set(key: String, value: FluidJson?): Unit = throwInvalidType("object")
    override operator fun set(index: Int, value: FluidJson?): Unit = throwInvalidType("array")
    override operator fun plusAssign(value: FluidJson?): Unit = throwInvalidType("array")

    override fun delete(key: String): Unit = throwInvalidType("object")
    override fun delete(index: Int): Unit = throwInvalidType("array")

    override val size: Int get() = throwInvalidType("object or array")

    override val isNull: Boolean get() = false
    override val orNull: FluidJson? get() = this

    override val stringOrNull: String? get() = null
    override val string: String get() = stringOrNull ?: throwInvalidType("string")

    override val intOrNull: Int? get() = null
    override val int: Int get() = intOrNull ?: throwInvalidType("int")

    override val longOrNull: Long? get() = null
    override val long: Long get() = longOrNull ?: throwInvalidType("long")

    override val doubleOrNull: Double? get() = null
    override val double: Double get() = doubleOrNull ?: throwInvalidType("double")

    override val boolOrNull: Boolean? get() = null
    override val bool: Boolean get() = boolOrNull ?: throwInvalidType("bool")

    override val arrayOrNull: List<FluidJson>? get() = null
    override val array: List<FluidJson> get() = arrayOrNull ?: throwInvalidType("array")

    override val objOrNull: Map<String, FluidJson>? get() = null
    override val obj: Map<String, FluidJson> get() = objOrNull ?: throwInvalidType("object")
}

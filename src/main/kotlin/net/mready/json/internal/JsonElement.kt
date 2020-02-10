@file:Suppress("NOTHING_TO_INLINE", "UNUSED_PARAMETER")

package net.mready.json.internal

import net.mready.json.FluidJson
import net.mready.json.FluidJsonException
import net.mready.json.JsonAdapter


sealed class JsonElement(path: JsonPath, adapter: JsonAdapter) : FluidJson(path, adapter) {
    abstract val elementName: String

    protected inline fun throwError(e: FluidJsonException): Nothing {
        throw e
    }

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
        return JsonError(
            e = FluidJsonException("Element $elementName is not an object", path),
            path = path + key,
            adapter = adapter
        )
    }

    override operator fun get(index: Int): FluidJson {
        return JsonError(
            e = FluidJsonException("Element $elementName is not an array", path),
            path = path + index,
            adapter = adapter
        )
    }

    override operator fun set(key: String, value: FluidJson?): Unit = throwInvalidType("object")
    override operator fun set(index: Int, value: FluidJson?): Unit = throwInvalidType("array")
    override operator fun plusAssign(value: FluidJson?): Unit = throwInvalidType("array")

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


class JsonNull(path: JsonPath = JsonPath.ROOT, adapter: JsonAdapter) : JsonElement(path, adapter) {
    override val elementName = "null"

    override fun copy(path: JsonPath, adapter: JsonAdapter) = JsonNull(path, adapter)

    override val isNull: Boolean get() = true
    override val orNull: JsonElement? get() = null
}

class JsonObject(
    internal val content: MutableMap<String, FluidJson>,
    path: JsonPath = JsonPath.ROOT,
    adapter: JsonAdapter
) : JsonElement(path, adapter) {
    override val elementName = "object"

    override fun copy(path: JsonPath, adapter: JsonAdapter) = JsonObject(
        content = content.mapValuesTo(mutableMapOf()) { it.value.copy(path + it.key, adapter) },
        path = path,
        adapter = adapter
    )

    override val size: Int get() = content.size

    override operator fun get(key: String): FluidJson {
        return content.getOrPut(key) {
            JsonEmpty(path + key, adapter) {
                FluidJsonException("No such key \"$key\" in object", path)
            }
        }
    }

    override operator fun set(key: String, value: FluidJson?) {
        val childPath = path + key
        content[key] = jsonNullOr(value, childPath, adapter) { it.copyIfNeeded(childPath, adapter) }
    }

    override val objOrNull: Map<String, FluidJson>? get() = content
}

class JsonArray(
    internal val content: MutableList<FluidJson>,
    path: JsonPath = JsonPath.ROOT,
    adapter: JsonAdapter
) : JsonElement(path, adapter) {
    override val elementName = "array"

    override fun copy(path: JsonPath, adapter: JsonAdapter) = JsonArray(
        content = content.mapIndexedTo(mutableListOf()) { index, item -> item.copy(path + index, adapter) },
        path = path,
        adapter = adapter
    )

    override val size: Int get() = content.size

    override operator fun get(index: Int): FluidJson {
        when {
            index < 0 -> throwError(
                FluidJsonException("Invalid array index $index", path)
            )
            index >= content.size -> {
                for (i in content.size..index) {
                    content.add(JsonEmpty(path + i, adapter) {
                        FluidJsonException("Index $index out of bounds (size: ${content.size})", path)
                    })
                }
            }
        }
        return content[index]
    }

    override operator fun set(index: Int, value: FluidJson?) {
        val childPath = path + index
        val newValue = jsonNullOr(value, childPath, adapter) { it.copyIfNeeded(childPath, adapter) }

        when {
            index < 0 -> throwError(
                FluidJsonException("Invalid array index $index", path)
            )
            index < content.size -> content[index] = newValue
            else -> {
                for (i in content.size until index) {
                    content.add(JsonEmpty(path + i, adapter))
                }
                content.add(newValue)
            }
        }
    }

    override operator fun plusAssign(value: FluidJson?) {
        set(size, value)
    }

    override val arrayOrNull: List<FluidJson>? get() = content
}

class JsonPrimitive(
    internal val content: String,
    internal val type: Type,
    path: JsonPath = JsonPath.ROOT,
    adapter: JsonAdapter
) : JsonElement(path, adapter) {
    override val elementName: String
        get() = when {
            isBool() -> "bool"
            isNumber() -> "number"
            isString() -> "string"
            else -> "unknown"
        }

    override fun copy(path: JsonPath, adapter: JsonAdapter) = JsonPrimitive(content, type, path, adapter)

    enum class Type {
        STRING, NUMBER, BOOLEAN, UNKNOWN
    }

    fun isBool(): Boolean = when (type) {
        Type.BOOLEAN -> true
        Type.UNKNOWN -> content == "true" || content == "false"
        else -> false
    }

    fun isString(): Boolean = when (type) {
        Type.STRING, Type.UNKNOWN -> true
        else -> false
    }

    fun isNumber(): Boolean = when (type) {
        Type.NUMBER -> true
        Type.UNKNOWN -> content.toDoubleOrNull() != null
        else -> false
    }

    override val stringOrNull: String?
        get() = if (type == Type.STRING || type == Type.UNKNOWN) {
            content
        } else {
            null
        }

    override val intOrNull: Int?
        get() = if (type == Type.NUMBER || type == Type.UNKNOWN) {
            content.toIntOrNull()
        } else {
            null
        }


    override val longOrNull: Long?
        get() = if (type == Type.NUMBER || type == Type.UNKNOWN) {
            content.toLongOrNull()
        } else {
            null
        }

    override val doubleOrNull: Double?
        get() = if (type == Type.NUMBER || type == Type.UNKNOWN) {
            content.toDoubleOrNull()
        } else {
            null
        }

    override val boolOrNull: Boolean?
        get() = if (isBool()) {
            content.toBoolean()
        } else {
            null
        }
}

class JsonReference(
    internal val content: Any,
    path: JsonPath = JsonPath.ROOT,
    adapter: JsonAdapter
) : JsonElement(path, adapter) {
    override val elementName: String
        get() = content::class.simpleName ?: "reference"

    override fun copy(path: JsonPath, adapter: JsonAdapter) = JsonReference(content, path, adapter)
}

class JsonError(
    private val e: FluidJsonException,
    path: JsonPath = JsonPath.ROOT,
    adapter: JsonAdapter
) : JsonElement(path, adapter) {
    override val elementName: String
        get() = "error (${e.message})"

    override fun copy(path: JsonPath, adapter: JsonAdapter) = JsonError(e, path, adapter)

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

class JsonEmpty(
    path: JsonPath = JsonPath.ROOT,
    adapter: JsonAdapter,
    private val pendingException: (() -> FluidJsonException)? = null
) : JsonElement(path, adapter) {
    override val elementName: String
        get() = (wrapped as? JsonElement)?.elementName ?: "null"

    @PublishedApi
    internal var wrapped: FluidJson? = null
    private inline val defaultException get() = FluidJsonException("Json element is empty", path)

    override fun copy(path: JsonPath, adapter: JsonAdapter) = wrapped?.copy(path, adapter) ?: JsonEmpty(path, adapter, pendingException)

    override val size: Int get() = wrapped?.size ?: 0

    private fun materializeAsObject(): FluidJson {
        if (wrapped == null) {
            wrapped = JsonObject(mutableMapOf(), path, adapter)
        } else if (wrapped !is JsonObject) {
            throwInvalidType("object")
        }

        return wrapped!!
    }

    private fun materializeAsArray(): FluidJson {
        if (wrapped == null) {
            wrapped = JsonArray(mutableListOf(), path, adapter)
        } else if (wrapped !is JsonArray) {
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
}
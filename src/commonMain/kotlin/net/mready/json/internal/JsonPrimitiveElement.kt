package net.mready.json.internal

import net.mready.json.JsonAdapter

class JsonPrimitiveElement(
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

    override fun copy(path: JsonPath, adapter: JsonAdapter) = JsonPrimitiveElement(content, type, path, adapter)

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JsonPrimitiveElement) return false

        if (this.isBool() && other.isBool()) {
            return this.bool == other.bool
        }

        if (this.isNumber() && other.isNumber()) {
            return this.double == other.double
        }

        if (this.isString() && other.isString()) {
            return this.string == other.string
        }

        return false
    }

    override fun hashCode(): Int {
        return content.hashCode()
    }
}
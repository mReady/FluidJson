package net.mready.json.internal

import net.mready.json.JsonAdapter

class JsonNullElement(path: JsonPath = JsonPath.ROOT, adapter: JsonAdapter) : JsonElement(path, adapter) {
    override val elementName = "null"

    override fun copy(path: JsonPath, adapter: JsonAdapter) = JsonNullElement(path, adapter)

    override val isNull: Boolean get() = true
    override val orNull: JsonElement? get() = null

    override fun equals(other: Any?): Boolean {
        return other is JsonNullElement
    }

    override fun hashCode(): Int {
        return 0
    }
}
package net.mready.json.internal

import net.mready.json.FluidJson
import net.mready.json.FluidJsonException
import net.mready.json.JsonAdapter

class JsonObjectElement(
    internal val content: MutableMap<String, FluidJson>,
    path: JsonPath = JsonPath.ROOT,
    adapter: JsonAdapter
) : JsonElement(path, adapter) {
    override val elementName = "object"

    override fun copy(path: JsonPath, adapter: JsonAdapter) = JsonObjectElement(
        content = content.mapValuesTo(mutableMapOf()) { it.value.copy(path + it.key, adapter) },
        path = path,
        adapter = adapter
    )

    override val size: Int get() = objOrNull.size

    override operator fun get(key: String): FluidJson {
        return content.getOrPut(key) {
            JsonEmptyElement(path + key, adapter) {
                FluidJsonException("No such key \"$key\" in object", path)
            }
        }
    }

    override operator fun set(key: String, value: FluidJson?) {
        val childPath = path + key
        content[key] = value?.copyIfNeeded(childPath, adapter) ?: JsonNullElement(childPath, adapter)
    }

    override fun delete(key: String) {
        content.remove(key)
    }

    override val objOrNull: Map<String, FluidJson> get() = content.filterValues {
        if (it is JsonEmptyElement) {
            it.wrapped() != null
        } else {
            true
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other is JsonObjectElement) {
            return this.objOrNull == other.objOrNull
        }
        if (other is JsonEmptyElement) {
            return this == other.wrapped()
        }
        return false
    }

    override fun hashCode(): Int {
        return content.hashCode()
    }
}
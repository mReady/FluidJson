package net.mready.json.internal

import net.mready.json.FluidJson
import net.mready.json.FluidJsonException
import net.mready.json.JsonAdapter

class JsonArrayElement(
    internal val content: MutableList<FluidJson>,
    path: JsonPath = JsonPath.ROOT,
    adapter: JsonAdapter
) : JsonElement(path, adapter) {
    override val elementName = "array"

    override fun copy(path: JsonPath, adapter: JsonAdapter) = JsonArrayElement(
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
                    content.add(JsonEmptyElement(path + i, adapter) {
                        FluidJsonException("Index $index out of bounds (size: ${content.size})", path)
                    })
                }
            }
        }
        return content[index]
    }

    override operator fun set(index: Int, value: FluidJson?) {
        val childPath = path + index
        val newValue = value?.copyIfNeeded(childPath, adapter) ?: JsonNullElement(childPath, adapter)

        when {
            index < 0 -> throwError(
                FluidJsonException("Invalid array index $index", path)
            )
            index < content.size -> content[index] = newValue
            else -> {
                for (i in content.size until index) {
                    content.add(JsonEmptyElement(path + i, adapter))
                }
                content.add(newValue)
            }
        }
    }

    override operator fun plusAssign(value: FluidJson?) {
        set(size, value)
    }

    override fun delete(index: Int) {
        content.removeAt(index)
    }

    override val arrayOrNull: List<FluidJson> get() = content

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is JsonArrayElement) {
            return this.content == other.content
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
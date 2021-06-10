package net.mready.json.internal

import net.mready.json.FluidJson
import net.mready.json.JsonAdapter
import kotlin.reflect.KType

class JsonRefElement(
    content: Any,
    val type: KType,
    path: JsonPath = JsonPath.ROOT,
    adapter: JsonAdapter
) : JsonElement(path, adapter) {
    override val elementName: String
        get() = content?.let { it::class.simpleName } ?: wrapped.elementName

    @PublishedApi
    internal var content: Any? = content

    @PublishedApi
    internal val wrapped: JsonElement by lazy {
        val json = adapter.toJson(content, type).copyIfNeeded(path, adapter) as JsonElement
        this.content = null
        return@lazy json
    }

    inline fun <T> select(valueTransform: (value: Any) -> T, jsonTransform: (json: FluidJson) -> T): T {
        return if (content != null) {
            valueTransform(content!!)
        } else {
            jsonTransform(wrapped)
        }
    }

    override fun copy(path: JsonPath, adapter: JsonAdapter) = select(
        valueTransform = {
            JsonRefElement(it, type, path, adapter)
        },
        jsonTransform = {
            it.copy(path, adapter)
        }
    )

    override fun get(key: String) = wrapped[key]
    override fun get(index: Int) = wrapped[index]

    override fun set(key: String, value: FluidJson?) {
        wrapped[key] = value
    }

    override fun set(index: Int, value: FluidJson?) {
        wrapped[index] = value
    }

    override fun plusAssign(value: FluidJson?) {
        wrapped += value
    }

    override fun delete(key: String) = wrapped.delete(key)
    override fun delete(index: Int) = wrapped.delete(index)

    override val size get() = wrapped.size
    override val isNull get() = wrapped.isNull
    override val orNull get() = wrapped.orNull
    override val stringOrNull get() = wrapped.stringOrNull
    override val string get() = wrapped.string
    override val intOrNull get() = wrapped.intOrNull
    override val int get() = wrapped.int
    override val longOrNull get() = wrapped.longOrNull
    override val long get() = wrapped.long
    override val doubleOrNull get() = wrapped.doubleOrNull
    override val double get() = wrapped.double
    override val boolOrNull get() = wrapped.boolOrNull
    override val bool get() = wrapped.bool
    override val arrayOrNull get() = wrapped.arrayOrNull
    override val array get() = wrapped.array
    override val objOrNull get() = wrapped.objOrNull
    override val obj get() = wrapped.obj
}
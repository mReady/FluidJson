package net.mready.json.internal

import net.mready.json.FluidJson
import net.mready.json.JsonAdapter
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class JsonRefElement(
    content: Any,
    val type: KType,
    path: JsonPath = JsonPath.ROOT,
    adapter: JsonAdapter
) : JsonElement(path, adapter) {
    override val elementName: String
        get() = content?.let { it::class.simpleName } ?: unwrapped.elementName

    @PublishedApi
    internal var content: Any? = content

    @PublishedApi
    internal val unwrapped: JsonElement by lazy {
        val json = adapter.toJson(content, type).copyIfNeeded(path, adapter) as JsonElement
        this.content = null
        return@lazy json
    }

    @PublishedApi
    internal fun isUnwrapped() = content == null

    inline fun <reified T, R> select(valueTransform: (value: T) -> R, jsonTransform: (json: FluidJson) -> R): R {
        return if (!isUnwrapped() && content is T && typeOf<T>().arguments.isEmpty()) {
            valueTransform(content as T)
        } else {
            jsonTransform(unwrapped)
        }
    }

    override fun copy(path: JsonPath, adapter: JsonAdapter) = select<Any, FluidJson>(
        valueTransform = {
            JsonRefElement(it, type, path, adapter)
        },
        jsonTransform = {
            it.copy(path, adapter)
        }
    )

    override fun get(key: String) = unwrapped[key]
    override fun get(index: Int) = unwrapped[index]

    override fun set(key: String, value: FluidJson?) {
        unwrapped[key] = value
    }

    override fun set(index: Int, value: FluidJson?) {
        unwrapped[index] = value
    }

    override fun plusAssign(value: FluidJson?) {
        unwrapped += value
    }

    override fun delete(key: String) = unwrapped.delete(key)
    override fun delete(index: Int) = unwrapped.delete(index)

    override val size get() = unwrapped.size
    override val isNull get() = unwrapped.isNull
    override val orNull get() = unwrapped.orNull
    override val stringOrNull get() = unwrapped.stringOrNull
    override val string get() = unwrapped.string
    override val intOrNull get() = unwrapped.intOrNull
    override val int get() = unwrapped.int
    override val longOrNull get() = unwrapped.longOrNull
    override val long get() = unwrapped.long
    override val doubleOrNull get() = unwrapped.doubleOrNull
    override val double get() = unwrapped.double
    override val boolOrNull get() = unwrapped.boolOrNull
    override val bool get() = unwrapped.bool
    override val arrayOrNull get() = unwrapped.arrayOrNull
    override val array get() = unwrapped.array
    override val objOrNull get() = unwrapped.objOrNull
    override val obj get() = unwrapped.obj
}
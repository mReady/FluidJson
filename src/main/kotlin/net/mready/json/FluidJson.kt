@file:Suppress("NOTHING_TO_INLINE", "UNUSED_PARAMETER")

package net.mready.json

import kotlinx.serialization.Serializable
import net.mready.json.adapters.FluidJsonSerializer
import net.mready.json.internal.JsonEmpty
import net.mready.json.internal.JsonNull
import net.mready.json.internal.JsonPath
import net.mready.json.internal.wrapValue

class FluidJsonException(
    message: String,
    val path: JsonPath? = null,
    cause: Throwable? = null
) : RuntimeException("$message ${path?.let { " (at $path)" }.orEmpty()}", cause)

@Serializable(with = FluidJsonSerializer::class)
abstract class FluidJson internal constructor(internal val path: JsonPath, internal val adapter: JsonAdapter) {
    companion object {
        fun parse(string: String, adapter: JsonAdapter = defaultJsonAdapter) = adapter.parse(string)

        operator fun invoke(adapter: JsonAdapter = defaultJsonAdapter): FluidJson =
            JsonEmpty(JsonPath.ROOT, adapter)

        operator fun invoke(value: Nothing?, adapter: JsonAdapter = defaultJsonAdapter): FluidJson =
            JsonNull(JsonPath.ROOT, adapter)

        operator fun invoke(value: String?, adapter: JsonAdapter = defaultJsonAdapter): FluidJson =
            wrapValue(value, JsonPath.ROOT, adapter)

        operator fun invoke(value: Number?, adapter: JsonAdapter = defaultJsonAdapter): FluidJson =
            wrapValue(value, JsonPath.ROOT, adapter)

        operator fun invoke(value: Boolean?, adapter: JsonAdapter = defaultJsonAdapter): FluidJson =
            wrapValue(value, JsonPath.ROOT, adapter)
    }

    internal abstract fun copy(path: JsonPath, adapter: JsonAdapter): FluidJson
    internal abstract fun copyIfNeeded(path: JsonPath, adapter: JsonAdapter): FluidJson

    abstract operator fun get(key: String): FluidJson
    abstract operator fun get(index: Int): FluidJson

    abstract operator fun set(key: String, value: FluidJson?)
    abstract operator fun set(index: Int, value: FluidJson?)
    abstract operator fun plusAssign(value: FluidJson?)

    abstract val size: Int

    abstract val isNull: Boolean
    abstract val orNull: FluidJson?

    abstract val stringOrNull: String?
    abstract val string: String

    abstract val intOrNull: Int?
    abstract val int: Int

    abstract val longOrNull: Long?
    abstract val long: Long

    abstract val doubleOrNull: Double?
    abstract val double: Double

    abstract val boolOrNull: Boolean?
    abstract val bool: Boolean

    abstract val arrayOrNull: List<FluidJson>?
    abstract val array: List<FluidJson>

    abstract val objOrNull: Map<String, FluidJson>?
    abstract val obj: Map<String, FluidJson>

    fun toJsonString(prettyPrint: Boolean = false, adapter: JsonAdapter = defaultJsonAdapter): String {
        return adapter.stringify(this, prettyPrint)
    }


    private fun Any?.toJson(path: JsonPath): FluidJson {
        return wrapValue(this, path, adapter)
    }

    // "extension" operators (embedded here because auto-import doesn't work great for operators)
    operator fun set(key: String, value: Nothing?) = set(key, JsonNull(path + key, adapter))

    operator fun set(key: String, value: String?) = set(key, value.toJson(path + key))
    operator fun set(key: String, value: Number?) = set(key, value.toJson(path + key))
    operator fun set(key: String, value: Boolean?) = set(key, value.toJson(path + key))

    @JvmName("setValues")
    operator fun set(key: String, value: Collection<FluidJson?>?) = set(key, value.toJson(path + key))

    @JvmName("setStrings")
    operator fun set(key: String, value: Collection<String?>?) = set(key, value.toJson(path + key))

    @JvmName("setNumbers")
    operator fun set(key: String, value: Collection<Number?>?) = set(key, value.toJson(path + key))

    operator fun set(index: Int, value: Nothing?) = set(index, JsonNull(path + index, adapter))
    operator fun set(index: Int, value: String?) = set(index, value.toJson(path + index))
    operator fun set(index: Int, value: Number?) = set(index, value.toJson(path + index))
    operator fun set(index: Int, value: Boolean?) = set(index, value.toJson(path + index))

    @JvmName("setValues")
    operator fun set(index: Int, value: Collection<FluidJson>?) = set(index, value.toJson(path + index))

    @JvmName("setStrings")
    operator fun set(index: Int, value: Collection<String?>?) = set(index, value.toJson(path + index))

    @JvmName("setNumbers")
    operator fun set(index: Int, value: Collection<Number?>?) = set(index, value.toJson(path + index))

    operator fun plusAssign(value: Nothing?) = plusAssign(JsonNull(path + size, adapter))
    operator fun plusAssign(value: String?) = plusAssign(value.toJson(path + size))
    operator fun plusAssign(value: Number?) = plusAssign(value.toJson(path + size))
    operator fun plusAssign(value: Boolean?) = plusAssign(value.toJson(path + size))

    @JvmName("plusValues")
    operator fun plusAssign(value: Collection<FluidJson>?) = plusAssign(value.toJson(path + size))

    @JvmName("plusStrings")
    operator fun plusAssign(value: Collection<String?>?) = plusAssign(value.toJson(path + size))

    @JvmName("plusNumbers")
    operator fun plusAssign(value: Collection<Number?>?) = plusAssign(value.toJson(path + size))
}
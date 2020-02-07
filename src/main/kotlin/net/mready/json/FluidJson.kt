@file:Suppress("NOTHING_TO_INLINE", "UNUSED_PARAMETER")

package net.mready.json

import kotlinx.serialization.Serializable
import net.mready.json.internal.JsonEmpty
import net.mready.json.internal.JsonNull
import net.mready.json.internal.wrapValue
import net.mready.json.adapters.FluidJsonSerializer

class FluidJsonException(
    message: String,
    val path: JsonPath? = null,
    cause: Throwable? = null
) : RuntimeException("$message ${path?.let { " (at $path)" }.orEmpty()}", cause)

inline class JsonPath(private val path: String) {
    companion object {
        @JvmStatic
        val ROOT = JsonPath("[root]")
    }

    operator fun plus(key: String) = JsonPath("$path > $key")
    operator fun plus(index: Int) = JsonPath("$path > [$index]")

    override fun toString() = path
}

@Serializable(with = FluidJsonSerializer::class)
abstract class FluidJson internal constructor(internal val path: JsonPath) {
    companion object {
        fun parse(string: String, adapter: JsonAdapter = defaultJsonAdapter) = adapter.parse(string)

        operator fun invoke(): FluidJson = JsonEmpty()

        @Suppress("UNUSED_PARAMETER")
        operator fun invoke(value: Nothing?): FluidJson = JsonNull()

        operator fun invoke(value: String?): FluidJson = wrapValue(value)
        operator fun invoke(value: Number?): FluidJson = wrapValue(value)
        operator fun invoke(value: Boolean?): FluidJson = wrapValue(value)
    }

    internal abstract fun copyWithPath(path: JsonPath): FluidJson

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

    // "extension" operators (embedded here because auto-import doesn't work great for operators)
    operator fun set(key: String, value: Nothing?) = set(key, JsonNull(path + key))

    operator fun set(key: String, value: String?) = set(key, wrapValue(value, path + key))
    operator fun set(key: String, value: Number?) = set(key, wrapValue(value, path + key))
    operator fun set(key: String, value: Boolean?) = set(key, wrapValue(value, path + key))

    @JvmName("setValues")
    operator fun set(key: String, value: Collection<FluidJson?>?) = set(key, wrapValue(value, path + key))

    @JvmName("setStrings")
    operator fun set(key: String, value: Collection<String?>?) = set(key, wrapValue(value, path + key))

    @JvmName("setNumbers")
    operator fun set(key: String, value: Collection<Number?>?) = set(key, wrapValue(value, path + key))

    operator fun set(index: Int, value: Nothing?) = set(index, JsonNull(path + index))
    operator fun set(index: Int, value: String?) = set(index, wrapValue(value, path + index))
    operator fun set(index: Int, value: Number?) = set(index, wrapValue(value, path + index))
    operator fun set(index: Int, value: Boolean?) = set(index, wrapValue(value, path + index))

    @JvmName("setValues")
    operator fun set(index: Int, value: Collection<FluidJson>?) = set(index, wrapValue(value, path + index))

    @JvmName("setStrings")
    operator fun set(index: Int, value: Collection<String?>?) = set(index, wrapValue(value, path + index))

    @JvmName("setNumbers")
    operator fun set(index: Int, value: Collection<Number?>?) = set(index, wrapValue(value, path + index))

    operator fun plusAssign(value: Nothing?) = plusAssign(JsonNull(path + size))
    operator fun plusAssign(value: String?) = plusAssign(wrapValue(value, path + size))
    operator fun plusAssign(value: Number?) = plusAssign(wrapValue(value, path + size))
    operator fun plusAssign(value: Boolean?) = plusAssign(wrapValue(value, path + size))

    @JvmName("plusValues")
    operator fun plusAssign(value: Collection<FluidJson>?) = plusAssign(wrapValue(value, path + size))

    @JvmName("plusStrings")
    operator fun plusAssign(value: Collection<String?>?) = plusAssign(wrapValue(value, path + size))

    @JvmName("plusNumbers")
    operator fun plusAssign(value: Collection<Number?>?) = plusAssign(wrapValue(value, path + size))
}
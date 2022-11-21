@file:Suppress("UNUSED_PARAMETER", "unused")

package net.mready.json

import kotlinx.atomicfu.atomic
import kotlinx.serialization.Serializable
import net.mready.json.adapters.FluidJsonSerializer
import net.mready.json.adapters.KotlinxJsonAdapter
import net.mready.json.adapters.isErased
import net.mready.json.internal.*
import kotlin.jvm.JvmName
import kotlin.reflect.KType
import kotlin.reflect.typeOf

typealias Json = FluidJson

@Serializable(with = FluidJsonSerializer::class)
abstract class FluidJson internal constructor(
    val path: JsonPath,
    val adapter: JsonAdapter
) {
    companion object : JsonAdapter() {
        private val defaultJsonAdapter = atomic<JsonAdapter?>(null)
        private val jsonAdapter: JsonAdapter by lazy {
            defaultJsonAdapter.compareAndSet(null, KotlinxJsonAdapter())
            defaultJsonAdapter.value!!
        }

        fun setDefaultAdapter(adapter: JsonAdapter) {
            if (!defaultJsonAdapter.compareAndSet(null, adapter)) {
                error("Default JsonAdapter already set")
            }
        }

        /**
         * Parse the given [string] using the default adapter.
         *
         * @throws [JsonParseException] if the given [string] is not a valid JSON
         */
        override fun parse(string: String) =
            jsonAdapter.parse(string)

        override fun stringify(json: FluidJson) =
            jsonAdapter.stringify(json)

        override fun <T> fromJson(json: FluidJson, type: KType) =
            jsonAdapter.fromJson<T>(json, type)

        override fun toJson(value: Any?, type: KType) =
            jsonAdapter.toJson(value, type)

        override fun <T> decodeObject(string: String, type: KType) =
            jsonAdapter.decodeObject<T>(string, type)

        override fun encodeObject(value: Any?, type: KType) =
            jsonAdapter.encodeObject(value, type)


        /**
         * Create an empty [FluidJson] instance.
         */
        operator fun invoke(): FluidJson = jsonAdapter.newJson()

        override fun equals(other: Any?): Boolean {
            // Setters and DSLs will make copies of elements if we don't override equals
            // but bad things can happen if the default adapter is switched after element instances were created
            return other == jsonAdapter
        }
    }

    /**
     * Creates a deep copy of this json tree using the specified [path] as root and [adapter].
     */
    abstract fun copy(path: JsonPath = this.path, adapter: JsonAdapter = this.adapter): FluidJson

    // Creates a copy only if the path or the adapter are different
    internal abstract fun copyIfNeeded(path: JsonPath, adapter: JsonAdapter): FluidJson

    /**
     * Returns the json element at the specified [key] if this json element represents an object.
     * If the key does not already exist in the object, a new [JsonEmptyElement] instance will be created that
     * can be materialized as an object or array.
     *
     * If this json element is not an object, a catch-all [JsonErrorElement] is returned that will throw when a
     * non-chaining operator is called
     */
    abstract operator fun get(key: String): FluidJson

    /**
     * Returns the json element at the specified [index] if this json element represents an array.
     * If the key does not already exist in the object, a new [JsonEmptyElement] instance will be created that
     * can be materialized as an object or array.
     *
     * If this json element is not an array, a catch-all [JsonErrorElement] is returned that will throw when a
     * non-chaining operator is called
     */
    abstract operator fun get(index: Int): FluidJson

    /**
     * Associates the specified [value] with the specified [key] if this json element represents an object.
     *
     * A copy of the specified [value] will be crated if the path is different from it's new position in the tree
     * or if it was bound to a different adapter.
     *
     * @throws [FluidJsonException] if this element does not represent an object
     */
    abstract operator fun set(key: String, value: FluidJson?)

    /**
     * Adds the specified [value] at the specified [index] if this json element represents an array.
     * In the [index] is > than the current [size] the "empty" indexes will be filled with [JsonEmptyElement] instances
     *
     * A copy of the specified [value] will be crated if the path is different from it's new position in the tree
     * or if it was bound to a different adapter.
     *
     * @throws [FluidJsonException] if this element does not represent an array
     */
    abstract operator fun set(index: Int, value: FluidJson?)

    /**
     * Adds the specified [value] if this json element represents an array.
     *
     * A copy of the specified [value] will be crated if the path is different from it's new position in the tree
     * or if it was bound to a different adapter.
     *
     * @throws [FluidJsonException] if this element does not represent an array
     */
    abstract operator fun plusAssign(value: FluidJson?)

    /**
     * Removes the specified [key] and its corresponding value if this json element represents an object.
     *
     * @throws [FluidJsonException] if this element does not represent an object
     */
    abstract fun delete(key: String)

    /**
     * Removes the element the specified [index] if this json element represents an array.
     *
     * @throws [FluidJsonException] if this element does not represent an array
     */
    abstract fun delete(index: Int)

    /**
     * Returns the number of items if this json element represents an object or array.
     *
     * @throws [FluidJsonException] if this element does not represent an object or array
     */
    abstract val size: Int

    /**
     * Returns `true` if this json element represents `null` or is an [JsonErrorElement].
     */
    abstract val isNull: Boolean

    /**
     * Returns `null` if this json element represents `null` or is an [JsonErrorElement].
     */
    abstract val orNull: FluidJson?

    /**
     * Returns the contained string if this json elements represents a string or `null` otherwise.
     */
    abstract val stringOrNull: String?

    /**
     * Returns the contained string if this json elements represents a string.
     *
     * @throws [FluidJsonException] if this element does not represent a string
     */
    abstract val string: String

    /**
     * Returns the contained number as int if this json elements represents a number or `null` otherwise.
     */
    abstract val intOrNull: Int?

    /**
     * Returns the contained number as int if this json elements represents a number.
     *
     * @throws [FluidJsonException] if this element does not represent a number
     */
    abstract val int: Int

    /**
     * Returns the contained number as long if this json elements represents a number or `null` otherwise.
     */
    abstract val longOrNull: Long?

    /**
     * Returns the contained number as long if this json elements represents a number.
     *
     * @throws [FluidJsonException] if this element does not represent a number
     */
    abstract val long: Long

    /**
     * Returns the contained number as double if this json elements represents a number or `null` otherwise.
     */
    abstract val doubleOrNull: Double?

    /**
     * Returns the contained number as double if this json elements represents a number.
     *
     * @throws [FluidJsonException] if this element does not represent a number
     */
    abstract val double: Double

    /**
     * Returns the contained boolean value if this json elements represents a boolean or `null` otherwise.
     */
    abstract val boolOrNull: Boolean?

    /**
     * Returns the contained boolean value if this json elements represents a boolean.
     *
     * @throws [FluidJsonException] if this element does not represent a boolean
     */
    abstract val bool: Boolean

    /**
     * Returns a list of the contained items if this json elements represents an array or `null` otherwise.
     */
    abstract val arrayOrNull: List<FluidJson>?

    /**
     * Returns a list of the contained items if this json elements represents an array.
     *
     * @throws [FluidJsonException] if this element does not represent an array
     */
    abstract val array: List<FluidJson>

    /**
     * Returns a map of the contained items if this json elements represents an object or `null` otherwise.
     */
    abstract val objOrNull: Map<String, FluidJson>?

    /**
     * Returns a map of the contained items if this json elements represents an object.
     *
     * @throws [FluidJsonException] if this element does not represent an object
     */
    abstract val obj: Map<String, FluidJson>

    /**
     * Constructs and returns the corresponding JSON string of this json tree.
     */
    fun toJsonString(): String {
        return adapter.stringify(this)
    }

    /**
     * Constructs and returns the corresponding JSON string of this json tree.
     */
    override fun toString(): String {
        return toJsonString()
    }

    private inline fun <reified T> T.asJson(path: JsonPath): FluidJson {
        return adapter.wrap(this, path)
    }

    private inline fun <reified T> Collection<T>?.asJsonArray(path: JsonPath): FluidJson {
        if (this == null) return JsonNullElement(path, adapter)
        val items = mapIndexedTo(mutableListOf()) { i, v -> adapter.wrap(v, path + i) }
        return JsonArrayElement(items, path, adapter)
    }

    private inline fun <reified T> Map<String, T>?.asJsonObject(path: JsonPath): FluidJson {
        if (this == null) return JsonNullElement(path, adapter)
        val items = mapValuesTo(mutableMapOf()) { item -> adapter.wrap(item.value, path + item.key) }
        return JsonObjectElement(items, path, adapter)
    }

    // "extension" operators (embedded here because auto-import doesn't work great for operators)

    /**
     * Associates a json element representing `null` with the specified [key].
     *
     * @throws [FluidJsonException] if this element does not represent an object
     */
    operator fun set(key: String, value: Nothing?) = set(key, JsonNullElement(path + key, adapter))

    /**
     * Associates a json element representing the given [value] with the specified [key].
     *
     * @throws [FluidJsonException] if this element does not represent an object
     */
    operator fun set(key: String, value: String?) = set(key, value.asJson(path + key))

    /**
     * Associates a json element representing the given [value] with the specified [key].
     *
     * @throws [FluidJsonException] if this element does not represent an object
     */
    operator fun set(key: String, value: Number?) = set(key, value.asJson(path + key))

    /**
     * Associates a json element representing the given [value] with the specified [key].
     *
     * @throws [FluidJsonException] if this element does not represent an object
     */
    operator fun set(key: String, value: Boolean?) = set(key, value.asJson(path + key))

    /**
     * Associates a json element representing the given [value] with the specified [key].
     *
     * @throws [FluidJsonException] if this element does not represent an object
     */
    @JvmName("setValues")
    operator fun set(key: String, value: Collection<FluidJson?>?) = set(key, value.asJsonArray(path + key))

    /**
     * Associates a json element representing the given [value] with the specified [key].
     *
     * @throws [FluidJsonException] if this element does not represent an object
     */
    @JvmName("setStrings")
    operator fun set(key: String, value: Collection<String?>?) = set(key, value.asJsonArray(path + key))

    /**
     * Associates a json element representing the given [value] with the specified [key].
     *
     * @throws [FluidJsonException] if this element does not represent an object
     */
    @JvmName("setNumbers")
    operator fun set(key: String, value: Collection<Number?>?) = set(key, value.asJsonArray(path + key))

    /**
     * Adds a json element representing `null` the the specified [index].
     *
     * @throws [FluidJsonException] if this element does not represent an array
     */
    operator fun set(index: Int, value: Nothing?) = set(index, JsonNullElement(path + index, adapter))

    /**
     * Adds a json element representing the given [value] the the specified [index].
     *
     * @throws [FluidJsonException] if this element does not represent an array
     */
    operator fun set(index: Int, value: String?) = set(index, value.asJson(path + index))

    /**
     * Adds a json element representing the given [value] the the specified [index].
     *
     * @throws [FluidJsonException] if this element does not represent an array
     */
    operator fun set(index: Int, value: Number?) = set(index, value.asJson(path + index))

    /**
     * Adds a json element representing the given [value] the the specified [index].
     *
     * @throws [FluidJsonException] if this element does not represent an array
     */
    operator fun set(index: Int, value: Boolean?) = set(index, value.asJson(path + index))

    /**
     * Adds a json element representing the given [value] the the specified [index].
     *
     * @throws [FluidJsonException] if this element does not represent an array
     */
    @JvmName("setValues")
    operator fun set(index: Int, value: Collection<FluidJson>?) = set(index, value.asJsonArray(path + index))

    /**
     * Adds a json element representing the given [value] the the specified [index].
     *
     * @throws [FluidJsonException] if this element does not represent an array
     */
    @JvmName("setStrings")
    operator fun set(index: Int, value: Collection<String?>?) = set(index, value.asJsonArray(path + index))

    /**
     * Adds a json element representing the given [value] the the specified [index].
     *
     * @throws [FluidJsonException] if this element does not represent an array
     */
    @JvmName("setNumbers")
    operator fun set(index: Int, value: Collection<Number?>?) = set(index, value.asJsonArray(path + index))

    /**
     * Adds a json element representing `null`.
     *
     * @throws [FluidJsonException] if this element does not represent an array
     */
    operator fun plusAssign(value: Nothing?) = plusAssign(JsonNullElement(path + size, adapter))

    /**
     * Adds a json element representing the given [value].
     *
     * @throws [FluidJsonException] if this element does not represent an array
     */
    operator fun plusAssign(value: String?) = plusAssign(value.asJson(path + size))

    /**
     * Adds a json element representing the given [value].
     *
     * @throws [FluidJsonException] if this element does not represent an array
     */
    operator fun plusAssign(value: Number?) = plusAssign(value.asJson(path + size))

    /**
     * Adds a json element representing the given [value].
     *
     * @throws [FluidJsonException] if this element does not represent an array
     */
    operator fun plusAssign(value: Boolean?) = plusAssign(value.asJson(path + size))

    /**
     * Adds a json element representing the given [value].
     *
     * @throws [FluidJsonException] if this element does not represent an array
     */
    @JvmName("plusValues")
    operator fun plusAssign(value: Collection<FluidJson>?) = plusAssign(value.asJsonArray(path + size))

    /**
     * Adds a json element representing the given [value].
     *
     * @throws [FluidJsonException] if this element does not represent an array
     */
    @JvmName("plusStrings")
    operator fun plusAssign(value: Collection<String?>?) = plusAssign(value.asJsonArray(path + size))

    /**
     * Adds a json element representing the given [value].
     *
     * @throws [FluidJsonException] if this element does not represent an array
     */
    @JvmName("plusNumbers")
    operator fun plusAssign(value: Collection<Number?>?) = plusAssign(value.asJsonArray(path + size))
}

inline fun <reified T : Any?> FluidJson.decodeOrNull(): T? {
    require(this is JsonElement)
    return when (this) {
        is JsonNullElement -> null
        is JsonErrorElement -> null
        is JsonRefElement -> select<T, T?>(
            valueTransform = { it },
            jsonTransform = {
                val type = typeOf<T>()
                val decodeType = if (type.isErased()) this.type else type
                runCatching { adapter.fromJson<T>(it, decodeType) }.getOrNull()
            }
        )
        is JsonArrayElement, is JsonObjectElement, is JsonPrimitiveElement -> runCatching {
            adapter.fromJson<T>(this, typeOf<T>())
        }.getOrNull()
        is JsonEmptyElement -> when (val wrapped = wrapped()) {
            null -> null
            else -> runCatching { adapter.fromJson<T>(wrapped, typeOf<T>()) }.getOrNull()
        }
    }
}

inline fun <reified T : Any?> FluidJson.decode(): T {
    require(this is JsonElement)
    return when (this) {
        is JsonNullElement -> null
        is JsonErrorElement -> throwError()
        is JsonRefElement -> {
            select<T, T>(
                valueTransform = { it },
                jsonTransform = {
                    val type = typeOf<T>()

                    val decodeType = if (type.isErased()) this.type else type
                    adapter.fromJson(it, decodeType)
                }
            )
        }
        is JsonArrayElement,
        is JsonObjectElement,
        is JsonPrimitiveElement
        -> adapter.fromJson<T>(this, typeOf<T>())

        is JsonEmptyElement -> when (val wrapped = wrapped()) {
            null -> null
            else -> adapter.fromJson<T>(wrapped, typeOf<T>())
        }
    } as T
}

class FluidJsonException(
    message: String,
    val path: JsonPath? = null,
    cause: Throwable? = null
) : RuntimeException("$message ${path?.let { " (at $path)" }.orEmpty()}", cause)

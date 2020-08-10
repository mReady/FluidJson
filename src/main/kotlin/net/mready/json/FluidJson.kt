@file:Suppress("UNUSED_PARAMETER", "unused")

package net.mready.json

import kotlinx.serialization.Serializable
import net.mready.json.adapters.FluidJsonSerializer
import net.mready.json.adapters.KotlinxJsonAdapter
import net.mready.json.internal.*
import kotlin.reflect.KType

typealias Json = FluidJson

@Serializable(with = FluidJsonSerializer::class)
abstract class FluidJson internal constructor(
    val path: JsonPath,
    val adapter: JsonAdapter
) {
    companion object: JsonAdapter() {
        private var defaultJsonAdapter: JsonAdapter = KotlinxJsonAdapter()

        fun setDefaultAdapter(adapter: JsonAdapter) {
            defaultJsonAdapter = adapter
        }

        /**
         * Parse the given [string] using the specified [adapter].
         *
         * @throws [JsonParseException] if the given [string] is not a valid JSON
         */
        override fun parse(string: String) =
            defaultJsonAdapter.parse(string)

        override fun stringify(json: FluidJson) =
            defaultJsonAdapter.stringify(json)

        @ExperimentalUserTypes
        override fun <T : Any> fromJson(json: FluidJson, type: KType) =
            defaultJsonAdapter.fromJson<T>(json, type)

        @ExperimentalUserTypes
        override fun toJson(value: Any?, type: KType) =
            defaultJsonAdapter.toJson(value, type)

        @ExperimentalUserTypes
        override fun <T : Any> decodeObject(string: String, type: KType) =
            defaultJsonAdapter.decodeObject<T>(string, type)

        @ExperimentalUserTypes
        override fun encodeObject(value: Any?, type: KType) =
            defaultJsonAdapter.encodeObject(value, type)


        /**
         * Create an empty [FluidJson] instance.
         */
        operator fun invoke(): FluidJson =
            defaultJsonAdapter.newJson()

        /**
         * Create a [FluidJson] instance representing `null`.
         */
        operator fun invoke(value: Nothing?): FluidJson =
            JsonNullElement(JsonPath.ROOT, defaultJsonAdapter)

        /**
         * Wrap the given string [value] into a [FluidJson] instance.
         */
        operator fun invoke(value: String?): FluidJson =
            defaultJsonAdapter.wrap(value, JsonPath.ROOT)

        /**
         * Wrap the given number [value] into a [FluidJson] instance.
         */
        operator fun invoke(value: Number?): FluidJson =
            defaultJsonAdapter.wrap(value, JsonPath.ROOT)

        /**
         * Wrap the given boolean [value] into a [FluidJson] instance.
         */
        operator fun invoke(value: Boolean?): FluidJson =
            defaultJsonAdapter.wrap(value, JsonPath.ROOT)

        override fun equals(other: Any?): Boolean {
            // Setters and DSLs will make copies of elements if we don't override equals
            // but bad things can happen if the default adapter is switched after element instances were created
            // TODO: should setting the default adapter be prohibited after it's first used?
            return other == defaultJsonAdapter
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

    private inline fun <reified T> T.asJson(path: JsonPath): FluidJson {
        return adapter.wrap(this, path)
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
    operator fun set(key: String, value: Collection<FluidJson?>?) = set(key, value.asJson(path + key))

    /**
     * Associates a json element representing the given [value] with the specified [key].
     *
     * @throws [FluidJsonException] if this element does not represent an object
     */
    @JvmName("setStrings")
    operator fun set(key: String, value: Collection<String?>?) = set(key, value.asJson(path + key))

    /**
     * Associates a json element representing the given [value] with the specified [key].
     *
     * @throws [FluidJsonException] if this element does not represent an object
     */
    @JvmName("setNumbers")
    operator fun set(key: String, value: Collection<Number?>?) = set(key, value.asJson(path + key))

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
    operator fun set(index: Int, value: Collection<FluidJson>?) = set(index, value.asJson(path + index))

    /**
     * Adds a json element representing the given [value] the the specified [index].
     *
     * @throws [FluidJsonException] if this element does not represent an array
     */
    @JvmName("setStrings")
    operator fun set(index: Int, value: Collection<String?>?) = set(index, value.asJson(path + index))

    /**
     * Adds a json element representing the given [value] the the specified [index].
     *
     * @throws [FluidJsonException] if this element does not represent an array
     */
    @JvmName("setNumbers")
    operator fun set(index: Int, value: Collection<Number?>?) = set(index, value.asJson(path + index))

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
    operator fun plusAssign(value: Collection<FluidJson>?) = plusAssign(value.asJson(path + size))

    /**
     * Adds a json element representing the given [value].
     *
     * @throws [FluidJsonException] if this element does not represent an array
     */
    @JvmName("plusStrings")
    operator fun plusAssign(value: Collection<String?>?) = plusAssign(value.asJson(path + size))

    /**
     * Adds a json element representing the given [value].
     *
     * @throws [FluidJsonException] if this element does not represent an array
     */
    @JvmName("plusNumbers")
    operator fun plusAssign(value: Collection<Number?>?) = plusAssign(value.asJson(path + size))
}

class FluidJsonException(
    message: String,
    val path: JsonPath? = null,
    cause: Throwable? = null
) : RuntimeException("$message ${path?.let { " (at $path)" }.orEmpty()}", cause)

package net.mready.json.internal

inline class JsonPath(private val path: String) {
    companion object {
        @JvmStatic
        val ROOT = JsonPath("[root]")
    }

    operator fun plus(key: String) = JsonPath("$path > $key")
    operator fun plus(index: Int) = JsonPath("$path > [$index]")

    override fun toString() = path
}
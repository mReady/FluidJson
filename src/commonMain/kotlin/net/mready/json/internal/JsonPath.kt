package net.mready.json.internal

import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmInline

@JvmInline
value class JsonPath(private val path: String) {
    companion object {
        @JvmStatic
        val ROOT = JsonPath("[json root]")
    }

    operator fun plus(key: String) = JsonPath("$path > $key")
    operator fun plus(index: Int) = JsonPath("$path > [$index]")

    override fun toString() = path
}
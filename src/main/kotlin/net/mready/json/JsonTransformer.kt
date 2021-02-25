package net.mready.json

import kotlin.reflect.KType

fun interface JsonTransformer {
    fun transform(value: Any?, type: KType, adapter: JsonAdapter): FluidJson?
}
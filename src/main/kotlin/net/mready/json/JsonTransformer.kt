package net.mready.json

fun interface JsonTransformer {
    fun transform(value: Any, adapter: JsonAdapter): FluidJson?
}
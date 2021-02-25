package net.mready.json

import net.mready.json.internal.JsonArrayElement
import net.mready.json.internal.JsonObjectElement

fun Collection<FluidJson>.toJsonArray(adapter: JsonAdapter = FluidJson): FluidJson {
    return JsonArrayElement(this.toMutableList(), adapter = adapter)
}

fun Map<String, FluidJson>.toJsonObject(adapter: JsonAdapter = FluidJson): FluidJson {
    return JsonObjectElement(this.toMutableMap(), adapter = adapter)
}
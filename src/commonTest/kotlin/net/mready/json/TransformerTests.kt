package net.mready.json

import net.mready.json.adapters.KotlinxJsonAdapter
import kotlin.test.Test
import kotlin.test.assertEquals

class TransformerTests {
    @Test
    fun test() {
        data class Wrapper(val inner: Int)

        val transformer = JsonTransformer { value, _, adapter ->
            if (value !is Wrapper) return@JsonTransformer null
            return@JsonTransformer adapter.wrap(value.inner)
        }

        val adapter = KotlinxJsonAdapter(transformers = setOf(transformer))

        val json = adapter.newJson()
        json["value"] = adapter.wrap(Wrapper(123))

        assertEquals(123, json["value"].int)
    }
}
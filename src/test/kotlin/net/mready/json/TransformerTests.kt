package net.mready.json

import net.mready.json.adapters.KotlinxJsonAdapter
import org.junit.Test
import kotlin.test.assertEquals

class TransformerTests {
    @Test
    fun test() {
        data class A(val a: Int)

        val transformer = JsonTransformer { value, adapter ->
            if (value !is A) return@JsonTransformer null
            return@JsonTransformer adapter.wrap(value.a)
        }

        val adapter = KotlinxJsonAdapter(transformers = setOf(transformer))

        val json = adapter.newJson()
        json["a"] = adapter.wrap(A(123))

        assertEquals(123, json["a"].int)
    }
}
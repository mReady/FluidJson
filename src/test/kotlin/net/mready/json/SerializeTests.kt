package net.mready.json

import net.mready.json.adapters.JacksonJsonAdapter
import net.mready.json.adapters.KotlinxJsonAdapter
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class SerializeTests(private val adapter: JsonAdapter) {
    companion object {
        @get:Parameterized.Parameters
        @JvmStatic
        val adapters = listOf(KotlinxJsonAdapter(), JacksonJsonAdapter)
    }

    @Test
    fun primitivesPreserveType() {
        val string = """{"int":1,"double":1.0,"bool":true,"string":"hello"}"""
        val json = adapter.parse(string)

        assertEquals(string, adapter.stringify(json))
    }
}
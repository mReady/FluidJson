package net.mready.json

import net.mready.json.kotlinx.KotlinxJsonAdapter
import org.junit.Test
import kotlin.test.assertEquals

class SerializeTests {
    private val adapter: JsonAdapter = KotlinxJsonAdapter()

    @Test
    fun primitivesPreserveType() {
        val string = """{"int":1,"double":1.0,"bool":true,"string":"hello"}"""
        val json = adapter.parse(string)

        assertEquals(string, adapter.stringify(json))
    }
}
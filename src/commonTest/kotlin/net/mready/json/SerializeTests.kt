package net.mready.json

import net.mready.json.adapters.KotlinxJsonAdapter
import kotlin.test.Test
import kotlin.test.assertEquals

open class SerializeTests {
    open val adapter: JsonAdapter = KotlinxJsonAdapter()

    @Test
    fun primitivesPreserveType() {
        val string = """{"int":1,"double":1.0,"bool":true,"string":"hello"}"""
        val json = adapter.parse(string)

        assertEquals(string, adapter.stringify(json))
    }
}
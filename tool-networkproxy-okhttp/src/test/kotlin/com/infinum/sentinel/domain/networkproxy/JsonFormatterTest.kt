package com.infinum.sentinel.domain.networkproxy

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

internal class JsonFormatterTest {
    @Test
    fun `object is spread over lines and keeps its content`() {
        val formatted = JsonFormatter.format("""{"b":2,"a":{"c":[1,2]}}""")

        assertTrue(formatted!!.contains("\n"))
        assertEquals(JSONObject("""{"b":2,"a":{"c":[1,2]}}""").toString(), JSONObject(formatted).toString())
    }

    @Test
    fun `array is spread over lines and keeps its content`() {
        val formatted = JsonFormatter.format("[1,2]")

        assertEquals("[\n  1,\n  2\n]", formatted)
        assertEquals("[1,2]", JSONArray(formatted).toString())
    }

    @Test
    fun `surrounding whitespace does not hide an array`() {
        val formatted = JsonFormatter.format("  \n[1,2]\n")

        assertTrue(formatted!!.startsWith("["))
    }

    @Test
    fun `text that is not json gives null`() {
        assertNull(JsonFormatter.format("not json"))
        assertNull(JsonFormatter.format("{\"unterminated\": "))
        assertNull(JsonFormatter.format(""))
    }
}

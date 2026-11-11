package com.infinum.sentinel.domain.networkproxy

import com.infinum.sentinel.domain.networkproxy.models.HttpMethod
import com.infinum.sentinel.domain.networkproxy.models.ProxyRule
import com.infinum.sentinel.domain.networkproxy.models.RuleMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal class RuleJsonTest {
    @Test
    fun `rules survive a round trip`() {
        val rules =
            listOf(
                ProxyRule(
                    id = "a",
                    enabled = false,
                    method = HttpMethod.POST,
                    pathPattern = "/users/*",
                    mode = RuleMode.MOCK,
                    statusCode = 404,
                    body = """{"error":"nope"}""",
                    contentType = "application/json",
                ),
                ProxyRule(id = "b", body = null),
            )

        assertEquals(rules, RuleJson.decode(RuleJson.encode(rules)))
    }

    @Test
    fun `null body stays null`() {
        val decoded = RuleJson.decode(RuleJson.encode(listOf(ProxyRule(id = "a", body = null))))
        assertEquals(null, decoded.single().body)
    }

    @Test
    fun `corrupt payload decodes to an empty list`() {
        assertTrue(RuleJson.decode("not json").isEmpty())
        assertTrue(RuleJson.decode("{\"unexpected\":true}").isEmpty())
    }

    @Test
    fun `null and blank payload decode to an empty list`() {
        assertTrue(RuleJson.decode(null).isEmpty())
        assertTrue(RuleJson.decode("   ").isEmpty())
    }

    @Test
    fun `entries without an id are skipped`() {
        val decoded = RuleJson.decode("""[{"pathPattern":"/x"},{"id":"kept","pathPattern":"/y"}]""")
        assertEquals(listOf("kept"), decoded.map { it.id })
    }

    @Test
    fun `unknown enum names fall back to defaults`() {
        val decoded = RuleJson.decode("""[{"id":"a","method":"TRACE","mode":"REPLAY"}]""").single()
        assertEquals(HttpMethod.ANY, decoded.method)
        assertEquals(RuleMode.MODIFY, decoded.mode)
    }
}

package com.infinum.sentinel.domain.networkproxy

import com.infinum.sentinel.domain.networkproxy.models.HttpMethod
import com.infinum.sentinel.domain.networkproxy.models.ProxyRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

internal class RuleMatcherTest {
    @Test
    fun `exact path matches`() {
        assertTrue(RuleMatcher.pathMatches("/users/me", "/users/me"))
    }

    @Test
    fun `different segment does not match`() {
        assertFalse(RuleMatcher.pathMatches("/users/me", "/users/you"))
    }

    @Test
    fun `single wildcard matches one segment`() {
        assertTrue(RuleMatcher.pathMatches("/users/*", "/users/42"))
        assertFalse(RuleMatcher.pathMatches("/users/*", "/users/42/posts"))
    }

    @Test
    fun `multiple wildcards match`() {
        assertTrue(RuleMatcher.pathMatches("/users/*/posts/*", "/users/42/posts/7"))
    }

    @Test
    fun `trailing slash is ignored on both sides`() {
        assertTrue(RuleMatcher.pathMatches("/users/", "/users"))
        assertTrue(RuleMatcher.pathMatches("/users", "/users/"))
    }

    @Test
    fun `segment count must be equal`() {
        assertFalse(RuleMatcher.pathMatches("/users", "/users/42"))
        assertFalse(RuleMatcher.pathMatches("/users/42", "/users"))
    }

    @Test
    fun `query string is ignored`() {
        assertTrue(RuleMatcher.pathMatches("/users?page=1", "/users"))
        assertTrue(RuleMatcher.pathMatches("/users", "/users?page=2"))
    }

    @Test
    fun `method ANY matches every method`() {
        val rule = ProxyRule(method = HttpMethod.ANY, pathPattern = "/users")
        assertTrue(RuleMatcher.matches(rule, "GET", "/users"))
        assertTrue(RuleMatcher.matches(rule, "DELETE", "/users"))
    }

    @Test
    fun `specific method matches case insensitively and rejects others`() {
        val rule = ProxyRule(method = HttpMethod.POST, pathPattern = "/users")
        assertTrue(RuleMatcher.matches(rule, "post", "/users"))
        assertFalse(RuleMatcher.matches(rule, "GET", "/users"))
    }

    @Test
    fun `first enabled match wins in list order`() {
        val disabled = ProxyRule(id = "disabled", enabled = false, pathPattern = "/users/*", statusCode = 500)
        val first = ProxyRule(id = "first", pathPattern = "/users/*", statusCode = 404)
        val second = ProxyRule(id = "second", pathPattern = "/users/42", statusCode = 400)
        val match = RuleMatcher.firstMatch(listOf(disabled, first, second), "GET", "/users/42")
        assertEquals("first", match?.id)
    }

    @Test
    fun `no match returns null`() {
        val rule = ProxyRule(pathPattern = "/users")
        assertNull(RuleMatcher.firstMatch(listOf(rule), "GET", "/posts"))
    }
}

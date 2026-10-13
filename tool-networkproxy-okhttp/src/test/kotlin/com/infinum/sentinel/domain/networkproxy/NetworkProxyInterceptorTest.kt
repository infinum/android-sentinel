package com.infinum.sentinel.domain.networkproxy

import com.infinum.sentinel.domain.networkproxy.models.HttpMethod
import com.infinum.sentinel.domain.networkproxy.models.ProxyRule
import com.infinum.sentinel.domain.networkproxy.models.RuleMode
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

internal class NetworkProxyInterceptorTest {
    private class FakeRuleSource(
        override var isEnabled: Boolean = true,
        override var rules: List<ProxyRule> = emptyList(),
    ) : RuleSource

    private val server = MockWebServer()
    private val ruleSource = FakeRuleSource()
    private val recentCalls = RecentCallStore()
    private val client =
        OkHttpClient
            .Builder()
            .addInterceptor(NetworkProxyInterceptor(ruleSource, recentCalls))
            .build()

    private val realBody = """{"real":true}"""

    @Before
    fun setUp() {
        server.start()
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `master switch off passes the real response through`() {
        ruleSource.isEnabled = false
        ruleSource.rules = listOf(ProxyRule(pathPattern = "/users", mode = RuleMode.MOCK, statusCode = 404, body = "{}"))
        enqueueReal()

        client.newCall(request("/users")).execute().use { response ->
            assertEquals(200, response.code)
            assertEquals(realBody, response.body.string())
        }
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `mock mode never reaches the server`() {
        ruleSource.rules =
            listOf(ProxyRule(pathPattern = "/users", mode = RuleMode.MOCK, statusCode = 404, body = """{"error":"nope"}"""))

        client.newCall(request("/users")).execute().use { response ->
            assertEquals(404, response.code)
            assertEquals("Not Found", response.message)
            assertEquals("""{"error":"nope"}""", response.body.string())
            assertEquals("application/json; charset=utf-8", response.header("Content-Type"))
        }
        assertEquals(0, server.requestCount)
        assertTrue(recentCalls.snapshot().single().mocked)
    }

    @Test
    fun `mock mode with null body returns an empty body`() {
        ruleSource.rules = listOf(ProxyRule(pathPattern = "/users", mode = RuleMode.MOCK, statusCode = 204, body = null))

        client.newCall(request("/users")).execute().use { response ->
            assertEquals(204, response.code)
            assertEquals("", response.body.string())
        }
    }

    @Test
    fun `modify mode changes the code and keeps the real body`() {
        ruleSource.rules = listOf(ProxyRule(pathPattern = "/users", mode = RuleMode.MODIFY, statusCode = 400, body = null))
        enqueueReal()

        client.newCall(request("/users")).execute().use { response ->
            assertEquals(400, response.code)
            assertEquals("Bad Request", response.message)
            assertEquals(realBody, response.body.string())
        }
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `modify mode replaces the body when the rule has one`() {
        ruleSource.rules =
            listOf(ProxyRule(pathPattern = "/users", mode = RuleMode.MODIFY, statusCode = 200, body = """{"replaced":true}"""))
        enqueueReal()

        client.newCall(request("/users")).execute().use { response ->
            assertEquals("""{"replaced":true}""", response.body.string())
            assertEquals("""{"replaced":true}""".length.toString(), response.header("Content-Length"))
        }
    }

    @Test
    fun `wildcard rule matches an id segment and ignores the query string`() {
        ruleSource.rules = listOf(ProxyRule(method = HttpMethod.ANY, pathPattern = "/users/*", mode = RuleMode.MOCK, statusCode = 418))

        client.newCall(request("/users/42?expand=true")).execute().use { response ->
            assertEquals(418, response.code)
        }
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `recent call is recorded and the caller still reads the body`() {
        enqueueReal()

        client.newCall(request("/users")).execute().use { response ->
            assertEquals(realBody, response.body.string())
        }

        val call = recentCalls.snapshot().single()
        assertEquals("GET", call.method)
        assertEquals("/users", call.path)
        assertEquals(200, call.statusCode)
        assertEquals(realBody, call.body)
        assertFalse(call.mocked)
    }

    @Test
    fun `modify mode records the real body and status, not the served ones`() {
        ruleSource.rules =
            listOf(ProxyRule(pathPattern = "/users", mode = RuleMode.MODIFY, statusCode = 500, body = """{"replaced":true}"""))
        enqueueReal()

        client.newCall(request("/users")).execute().use { response ->
            assertEquals(500, response.code)
            assertEquals("""{"replaced":true}""", response.body.string())
        }

        val call = recentCalls.snapshot().single()
        assertEquals(200, call.statusCode)
        assertEquals(realBody, call.body)
        assertTrue(call.mocked)
    }

    @Test
    fun `mock mode does not overwrite a previously captured real response`() {
        enqueueReal()
        client.newCall(request("/users")).execute().close()
        ruleSource.rules = listOf(ProxyRule(pathPattern = "/users", mode = RuleMode.MOCK, statusCode = 404, body = "{}"))

        client.newCall(request("/users")).execute().close()

        val call = recentCalls.snapshot().single()
        assertEquals(200, call.statusCode)
        assertEquals(realBody, call.body)
        assertTrue(call.mocked)
    }

    @Test
    fun `event stream bodies are not captured`() {
        server.enqueue(
            MockResponse
                .Builder()
                .code(200)
                .addHeader("Content-Type", "text/event-stream")
                .body("data: hello\n\n")
                .build(),
        )

        client.newCall(request("/events")).execute().close()

        assertEquals(null, recentCalls.snapshot().single().body)
    }

    @Test
    fun `binary bodies are recorded without a body`() {
        server.enqueue(
            MockResponse
                .Builder()
                .code(200)
                .addHeader("Content-Type", "application/octet-stream")
                .body("binary")
                .build(),
        )

        client.newCall(request("/file")).execute().close()

        assertEquals(null, recentCalls.snapshot().single().body)
    }

    private fun enqueueReal() {
        server.enqueue(
            MockResponse
                .Builder()
                .code(200)
                .addHeader("Content-Type", "application/json")
                .body(realBody)
                .build(),
        )
    }

    private fun request(path: String): Request = Request.Builder().url(server.url(path)).build()
}

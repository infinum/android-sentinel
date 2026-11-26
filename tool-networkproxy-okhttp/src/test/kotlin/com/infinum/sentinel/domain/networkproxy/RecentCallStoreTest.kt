package com.infinum.sentinel.domain.networkproxy

import com.infinum.sentinel.domain.networkproxy.models.HttpMethod
import com.infinum.sentinel.domain.networkproxy.models.RecentCall
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

internal class RecentCallStoreTest {
    private val store = RecentCallStore()

    @Test
    fun `findMatching resolves a wildcard pattern against a concrete path`() {
        store.record(call(method = "GET", path = "/users/42", body = "user"))

        assertEquals("user", store.findMatching(HttpMethod.GET, "/users/*")?.body)
        assertNull(store.findMatching(HttpMethod.GET, "/users"))
    }

    @Test
    fun `findMatching with ANY matches every method`() {
        store.record(call(method = "POST", path = "/users", body = "created"))

        assertEquals("created", store.findMatching(HttpMethod.ANY, "/users")?.body)
        assertNull(store.findMatching(HttpMethod.GET, "/users"))
    }

    @Test
    fun `markMocked keeps the real body and status of an existing entry`() {
        store.record(call(method = "GET", path = "/users", body = "real", statusCode = 200))

        store.markMocked("GET", "/users", statusCode = 404)

        val entry = store.snapshot().single()
        assertTrue(entry.mocked)
        assertEquals("real", entry.body)
        assertEquals(200, entry.statusCode)
    }

    @Test
    fun `markMocked without a prior call records a body-less entry`() {
        store.markMocked("GET", "/users", statusCode = 404)

        val entry = store.snapshot().single()
        assertTrue(entry.mocked)
        assertNull(entry.body)
        assertEquals(404, entry.statusCode)
    }

    @Test
    fun `listeners are notified on record, markMocked and clear`() {
        var notifications = 0
        store.addListener { notifications++ }

        store.record(call(method = "GET", path = "/users", body = null))
        store.markMocked("GET", "/users", statusCode = 200)
        store.clear()

        assertEquals(3, notifications)
    }

    private fun call(
        method: String,
        path: String,
        body: String?,
        statusCode: Int = 200,
    ): RecentCall =
        RecentCall(
            method = method,
            path = path,
            statusCode = statusCode,
            timestamp = 0L,
            body = body,
            contentType = null,
            mocked = false,
        )
}

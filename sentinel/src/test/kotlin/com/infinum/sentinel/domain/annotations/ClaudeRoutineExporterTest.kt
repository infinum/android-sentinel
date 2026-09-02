package com.infinum.sentinel.domain.annotations

import com.infinum.sentinel.data.models.local.annotations.Annotation
import com.infinum.sentinel.data.models.local.annotations.AnnotationToolkit
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

internal class ClaudeRoutineExporterTest {
    private val fireUrl = "https://api.anthropic.com/v1/claude_code/routines/trig_test/fire"

    private val annotations =
        listOf(
            Annotation(
                id = "a3k9x2m1",
                note = "button too small",
                toolkit = AnnotationToolkit.VIEW,
                targetType = "MaterialButton",
                targetPath = "MaterialButton",
            ),
        )

    private class RecordingPoster(private val response: HttpResponse) : HttpPoster {
        var url: String? = null
        var headers: Map<String, String> = emptyMap()
        var body: String? = null
        var calls: Int = 0

        override suspend fun post(url: String, headers: Map<String, String>, body: String): HttpResponse {
            this.url = url
            this.headers = headers
            this.body = body
            this.calls++
            return response
        }
    }

    @Test
    fun `sends the annotation json wrapped in a text field`() = runTest {
        val poster = RecordingPoster(HttpResponse(200, ""))
        val exporter = ClaudeRoutineExporter({ "sk-ant-test" }, fireUrl, poster)

        exporter.send(annotations)

        assertEquals(fireUrl, poster.url)
        assertTrue(poster.body!!.startsWith("""{"text":"["""))
        assertTrue(poster.body!!.contains("button too small"))
    }

    @Test
    fun `sends the documented authorization and beta headers`() = runTest {
        val poster = RecordingPoster(HttpResponse(200, ""))
        val exporter = ClaudeRoutineExporter({ "sk-ant-test" }, fireUrl, poster)

        exporter.send(annotations)

        assertEquals("Bearer sk-ant-test", poster.headers["Authorization"])
        assertEquals("2023-06-01", poster.headers["anthropic-version"])
        assertEquals("experimental-cc-routine-2026-04-01", poster.headers["anthropic-beta"])
        assertEquals("application/json", poster.headers["Content-Type"])
    }

    @Test
    fun `reports how many annotations were sent`() = runTest {
        val poster = RecordingPoster(HttpResponse(200, ""))
        val exporter = ClaudeRoutineExporter({ "sk-ant-test" }, fireUrl, poster)

        assertEquals(1, exporter.send(annotations))
    }

    @Test
    fun `a blank token fails without making a request`() = runTest {
        val poster = RecordingPoster(HttpResponse(200, ""))
        val exporter = ClaudeRoutineExporter({ "   " }, fireUrl, poster)

        try {
            exporter.send(annotations)
            fail("Expected an IllegalStateException")
        } catch (expected: IllegalStateException) {
            assertEquals(0, poster.calls)
        }
    }

    @Test
    fun `a null token fails without making a request`() = runTest {
        val poster = RecordingPoster(HttpResponse(200, ""))
        val exporter = ClaudeRoutineExporter({ null }, fireUrl, poster)

        try {
            exporter.send(annotations)
            fail("Expected an IllegalStateException")
        } catch (expected: IllegalStateException) {
            assertEquals(0, poster.calls)
        }
    }

    @Test
    fun `a non 2xx response throws with the status and body`() = runTest {
        val poster = RecordingPoster(HttpResponse(401, """{"error":"unauthorized"}"""))
        val exporter = ClaudeRoutineExporter({ "sk-ant-test" }, fireUrl, poster)

        try {
            exporter.send(annotations)
            fail("Expected an IllegalStateException")
        } catch (expected: IllegalStateException) {
            assertTrue(expected.message!!.contains("401"))
            assertTrue(expected.message!!.contains("unauthorized"))
        }
    }
}

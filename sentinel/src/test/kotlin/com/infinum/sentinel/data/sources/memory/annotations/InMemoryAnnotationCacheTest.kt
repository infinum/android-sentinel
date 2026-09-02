package com.infinum.sentinel.data.sources.memory.annotations

import com.infinum.sentinel.data.models.local.annotations.Annotation
import com.infinum.sentinel.data.models.local.annotations.AnnotationToolkit
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal class InMemoryAnnotationCacheTest {
    private fun annotation(note: String = "note") =
        Annotation(
            id = "",
            note = note,
            toolkit = AnnotationToolkit.VIEW,
            targetType = "MaterialButton",
            targetPath = "MaterialButton",
        )

    @Test
    fun `add assigns a non-empty id`() = runTest {
        val cache = InMemoryAnnotationCache()

        val stored = cache.add(annotation())

        assertTrue(stored.id.isNotEmpty())
        assertEquals(listOf(stored), cache.load().value)
    }

    @Test
    fun `add assigns unique ids`() = runTest {
        val cache = InMemoryAnnotationCache()

        val first = cache.add(annotation("first"))
        val second = cache.add(annotation("second"))

        assertNotEquals(first.id, second.id)
    }

    @Test
    fun `add keeps insertion order`() = runTest {
        val cache = InMemoryAnnotationCache()

        cache.add(annotation("first"))
        cache.add(annotation("second"))

        assertEquals(listOf("first", "second"), cache.load().value.map { it.note })
    }

    @Test
    fun `updateNote replaces text in place`() = runTest {
        val cache = InMemoryAnnotationCache()
        val stored = cache.add(annotation("before"))
        cache.add(annotation("other"))

        cache.updateNote(stored.id, "after")

        assertEquals(listOf("after", "other"), cache.load().value.map { it.note })
    }

    @Test
    fun `updateNote on an unknown id is a no-op`() = runTest {
        val cache = InMemoryAnnotationCache()
        cache.add(annotation("kept"))

        cache.updateNote("missing", "ignored")

        assertEquals(listOf("kept"), cache.load().value.map { it.note })
    }

    @Test
    fun `remove drops only the matching id`() = runTest {
        val cache = InMemoryAnnotationCache()
        val first = cache.add(annotation("first"))
        cache.add(annotation("second"))

        cache.remove(first.id)

        assertEquals(listOf("second"), cache.load().value.map { it.note })
    }

    @Test
    fun `identical notes are removed by id not by value`() = runTest {
        val cache = InMemoryAnnotationCache()
        val first = cache.add(annotation("same"))
        val second = cache.add(annotation("same"))

        cache.remove(second.id)

        assertEquals(listOf(first.id), cache.load().value.map { it.id })
    }

    @Test
    fun `markSent flags every unsent note`() = runTest {
        val cache = InMemoryAnnotationCache()
        cache.add(annotation("first"))
        cache.add(annotation("second"))

        cache.markSent()

        assertTrue(cache.load().value.all { it.isSent })
    }

    @Test
    fun `clear empties the cache`() = runTest {
        val cache = InMemoryAnnotationCache()
        cache.add(annotation())

        cache.clear()

        assertTrue(cache.load().value.isEmpty())
    }

    @Test
    fun `capture flag starts inactive`() {
        assertFalse(InMemoryAnnotationCache().active.value)
    }
}

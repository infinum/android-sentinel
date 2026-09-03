package com.infinum.sentinel.data.sources.memory.annotations

import com.infinum.sentinel.data.models.local.annotations.Annotation
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.tatarka.inject.annotations.Inject

@Inject
internal class InMemoryAnnotationCache : AnnotationCache {
    private companion object {
        private const val ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789"
        private const val ID_LENGTH = 8
    }

    private val annotations = mutableListOf<Annotation>()
    private val annotationsFlow = MutableStateFlow<List<Annotation>>(emptyList())

    override val active = MutableStateFlow(false)

    override fun load(): StateFlow<List<Annotation>> = annotationsFlow.asStateFlow()

    override suspend fun add(annotation: Annotation): Annotation {
        val stored = if (annotation.id.isEmpty()) annotation.copy(id = generateId()) else annotation
        stored.target = annotation.target
        annotations.add(stored)
        publish()
        return stored
    }

    override suspend fun updateNote(id: String, note: String) =
        replace(id) { it.copy(note = note) }

    override suspend fun rebind(id: String, target: Any?) {
        val index = annotations.indexOfFirst { it.id == id }
        if (index < 0) return
        annotations[index].target = target
        publish()
    }

    /**
     * Mutates `target` in place. `target` is a class-body property, so `copy()` would silently
     * drop it — every `copy()` in this class re-assigns it for exactly that reason, and this
     * path avoids the trap by never copying at all.
     */
    override fun releaseTargets(owned: (Annotation) -> Boolean) {
        annotations.forEach { annotation ->
            if (annotation.target != null && owned(annotation)) annotation.target = null
        }
    }

    override suspend fun remove(id: String) {
        if (annotations.removeAll { it.id == id }) publish()
    }

    override suspend fun markSent(ids: Collection<String>) {
        var changed = false
        annotations.forEachIndexed { index, annotation ->
            if (!annotation.isSent && annotation.id in ids) {
                val sent = annotation.copy(isSent = true)
                sent.target = annotation.target
                annotations[index] = sent
                changed = true
            }
        }
        if (changed) publish()
    }

    override suspend fun clear() {
        annotations.clear()
        publish()
    }

    private fun replace(id: String, transform: (Annotation) -> Annotation) {
        val index = annotations.indexOfFirst { it.id == id }
        if (index < 0) return
        val previous = annotations[index]
        val updated = transform(previous)
        updated.target = previous.target
        annotations[index] = updated
        publish()
    }

    private fun publish() {
        annotationsFlow.value = annotations.toList()
    }

    /** Short opaque id, unique among the notes currently stored. Nothing parses its shape. */
    private fun generateId(): String {
        var candidate: String
        do {
            candidate = (1..ID_LENGTH).map { ALPHABET[Random.nextInt(ALPHABET.length)] }.joinToString("")
        } while (annotations.any { it.id == candidate })
        return candidate
    }
}

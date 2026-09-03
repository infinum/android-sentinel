package com.infinum.sentinel.data.sources.memory.annotations

import com.infinum.sentinel.annotations.SentinelAnnotation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal interface AnnotationCache {
    /** Single source of truth for whether capture mode is armed. */
    val active: MutableStateFlow<Boolean>

    fun load(): StateFlow<List<SentinelAnnotation>>

    /** Stores [annotation], assigning an id when it has none. Returns the stored copy. */
    suspend fun add(annotation: SentinelAnnotation): SentinelAnnotation

    suspend fun updateNote(id: String, note: String)

    /** Re-points the note at a freshly created element after navigating back to its screen. */
    suspend fun rebind(id: String, target: Any?)

    /**
     * Drops the live handle of every stored note matching [owned], keeping the note itself.
     *
     * Not `suspend`: the caller is `onActivityDestroyed`, and the whole point is that the handle
     * is gone before that Activity would otherwise stay reachable from this process-lifetime
     * cache. Nothing observable changes — `target` is outside the note's `equals` — so this
     * publishes nothing.
     */
    fun releaseTargets(owned: (SentinelAnnotation) -> Boolean)

    suspend fun remove(id: String)

    /** Flags only the annotations whose id is in [ids] as sent. Everything else is untouched. */
    suspend fun markSent(ids: Collection<String>)

    suspend fun clear()
}

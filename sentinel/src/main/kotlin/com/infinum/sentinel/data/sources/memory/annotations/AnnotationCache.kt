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

    /** Replaces the note text. A changed text makes an exported note pending again. */
    suspend fun updateNote(id: String, note: String)

    /** Re-points the note at a freshly created element after navigating back to its screen. */
    suspend fun rebind(id: String, target: Any?)

    /**
     * Drops the live handle of every stored note matching [owned], keeping the note itself.
     *
     * Not `suspend`: it is called from `onActivityDestroyed` and must clear the handles before it
     * returns. `target` is outside the note's `equals`, so this publishes nothing.
     */
    fun releaseTargets(owned: (SentinelAnnotation) -> Boolean)

    suspend fun remove(id: String)

    /**
     * Flags the stored notes that still equal one of [exported] as sent. A note edited after the
     * snapshot was taken no longer matches, so it stays pending.
     */
    suspend fun markSent(exported: Collection<SentinelAnnotation>)

    suspend fun clear()
}

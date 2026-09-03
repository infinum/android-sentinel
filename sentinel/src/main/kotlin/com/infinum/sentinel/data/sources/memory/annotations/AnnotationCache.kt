package com.infinum.sentinel.data.sources.memory.annotations

import com.infinum.sentinel.data.models.local.annotations.Annotation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal interface AnnotationCache {
    /** Single source of truth for whether capture mode is armed. */
    val active: MutableStateFlow<Boolean>

    fun load(): StateFlow<List<Annotation>>

    /** Stores [annotation], assigning an id when it has none. Returns the stored copy. */
    suspend fun add(annotation: Annotation): Annotation

    suspend fun updateNote(id: String, note: String)

    /** Re-points the note at a freshly created element after navigating back to its screen. */
    suspend fun rebind(id: String, target: Any?)

    suspend fun remove(id: String)

    /** Flags only the annotations whose id is in [ids] as sent. Everything else is untouched. */
    suspend fun markSent(ids: Collection<String>)

    suspend fun clear()
}

package com.infinum.sentinel.ui.annotations

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.infinum.sentinel.R
import com.infinum.sentinel.annotations.AnnotationExporter
import com.infinum.sentinel.data.sources.memory.annotations.AnnotationCache
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.tatarka.inject.annotations.Inject

@Inject
internal class AnnotationsViewModel(
    private val cache: AnnotationCache,
    private val exporter: AnnotationExporter,
) : BaseChildViewModel<AnnotationsState, AnnotationsEvent>() {
    override fun data() {
        cache
            .load()
            .onEach { setState(AnnotationsState.Data(it)) }
            .launchIn(viewModelScope)
    }

    fun exporterIcon(): Int = exporter.icon()

    fun exporterLabel(): Int = exporter.label()

    /** Arms capture and asks the screen to close so the live app is visible. */
    fun arm() =
        launch {
            cache.active.value = true
            emitEvent(AnnotationsEvent.CaptureArmed)
        }

    fun delete(id: String) = launch { cache.remove(id) }

    fun clear() = launch { cache.clear() }

    /**
     * Exports the notes that have not been sent yet, then marks them sent. Reports the
     * exporter's own status message, or the failure it threw.
     */
    @Suppress("TooGenericExceptionCaught", "LabeledExpression")
    fun export(context: Context) =
        launch {
            val pending = cache.load().value.filterNot { it.isSent }
            // Guard clause: bail out before touching the exporter when there is nothing to send.
            if (pending.isEmpty()) {
                emitEvent(AnnotationsEvent.ExportFailed(context.getString(R.string.sentinel_annotations_empty_export)))
                return@launch
            }
            try {
                val message = exporter.export(context, pending)
                cache.markSent(pending.map { it.id })
                emitEvent(AnnotationsEvent.ExportFinished(message))
            } catch (error: Exception) {
                emitEvent(
                    AnnotationsEvent.ExportFailed(
                        context.getString(R.string.sentinel_annotations_export_failed, error.message.orEmpty()),
                    ),
                )
            }
        }
}

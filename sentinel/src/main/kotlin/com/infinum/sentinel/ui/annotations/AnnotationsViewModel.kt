package com.infinum.sentinel.ui.annotations

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.infinum.sentinel.R
import com.infinum.sentinel.annotations.AnnotationExporter
import com.infinum.sentinel.annotations.SentinelAnnotation
import com.infinum.sentinel.data.sources.memory.annotations.AnnotationCache
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.tatarka.inject.annotations.Inject

@Inject
internal class AnnotationsViewModel(
    private val cache: AnnotationCache,
    private val exporter: AnnotationExporter,
) : BaseChildViewModel<AnnotationsState, AnnotationsEvent>() {
    private val exporting = MutableStateFlow(false)

    override fun data() {
        combine(cache.load(), exporting) { annotations, isExporting -> AnnotationsState.Data(annotations, isExporting) }
            .onEach { setState(it) }
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
     *
     * Only one export runs at a time, so a second tap cannot send the same batch twice. Only the
     * exported snapshot is marked sent: a note edited while the export ran stays pending.
     */
    @Suppress("TooGenericExceptionCaught", "LabeledExpression")
    fun export(context: Context) {
        if (!exporting.compareAndSet(expect = false, update = true)) return
        launch {
            try {
                val all = cache.load().value
                val pending = all.filterNot { it.isSent }
                if (pending.isEmpty()) {
                    val reason =
                        if (all.isEmpty()) R.string.sentinel_annotations_empty_export else R.string.sentinel_annotations_all_exported
                    emitEvent(AnnotationsEvent.ExportFailed(context.getString(reason)))
                    return@launch
                }
                val json = io { SentinelAnnotation.toExportJson(pending) }
                val message = exporter.export(context, json, pending.size)
                cache.markSent(pending)
                emitEvent(AnnotationsEvent.ExportFinished(message))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                emitEvent(
                    AnnotationsEvent.ExportFailed(
                        context.getString(R.string.sentinel_annotations_export_failed, error.message.orEmpty()),
                    ),
                )
            } finally {
                exporting.value = false
            }
        }
    }
}

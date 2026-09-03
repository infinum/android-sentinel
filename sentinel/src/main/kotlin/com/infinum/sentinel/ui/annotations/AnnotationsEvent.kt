package com.infinum.sentinel.ui.annotations

internal sealed class AnnotationsEvent {
    /** Capture armed: the screen closes so the user lands back on the live app. */
    data object CaptureArmed : AnnotationsEvent()

    data class ExportFinished(
        val message: String,
    ) : AnnotationsEvent()

    data class ExportFailed(
        val message: String,
    ) : AnnotationsEvent()
}

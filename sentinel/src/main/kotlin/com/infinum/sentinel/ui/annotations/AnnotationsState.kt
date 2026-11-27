package com.infinum.sentinel.ui.annotations

import com.infinum.sentinel.annotations.SentinelAnnotation

internal sealed class AnnotationsState {
    data class Data(
        val annotations: List<SentinelAnnotation>,
        /** Whether an export is in flight. The export button stays disabled until it ends. */
        val isExporting: Boolean = false,
    ) : AnnotationsState()
}

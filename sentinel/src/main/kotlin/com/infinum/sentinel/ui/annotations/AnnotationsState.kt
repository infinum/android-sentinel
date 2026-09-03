package com.infinum.sentinel.ui.annotations

import com.infinum.sentinel.annotations.SentinelAnnotation

internal sealed class AnnotationsState {
    data class Data(
        val annotations: List<SentinelAnnotation>,
    ) : AnnotationsState()
}
